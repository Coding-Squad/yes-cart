/*
 * Copyright 2009 - 2016 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
import {Component, OnInit, OnDestroy, ViewChild} from '@angular/core';
import {NgIf} from '@angular/common';
import {HTTP_PROVIDERS}    from '@angular/http';
import {CatalogService, Util} from './../shared/services/index';
import {TAB_DIRECTIVES} from 'ng2-bootstrap/ng2-bootstrap';
import {CategoriesComponent, CategoryComponent} from './components/index';
import {DataControlComponent} from './../shared/sidebar/index';
import {CategorySelectComponent} from './../shared/catalog/index';
import {ModalComponent, ModalResult, ModalAction} from './../shared/modal/index';
import {CategoryVO, BasicCategoryVO, AttrValueCategoryVO, Pair} from './../shared/model/index';
import {FormValidationEvent, Futures, Future} from './../shared/event/index';
import {Config} from './../shared/config/env.config';

@Component({
  selector: 'yc-catalog-category',
  moduleId: module.id,
  templateUrl: 'catalog-category.component.html',
  directives: [TAB_DIRECTIVES, NgIf, CategoriesComponent, CategorySelectComponent, CategoryComponent, ModalComponent, DataControlComponent ],
})

export class CatalogCategoryComponent implements OnInit, OnDestroy {

  private static CATEGORIES:string = 'categories';
  private static CATEGORY:string = 'category';

  private forceShowAll:boolean = false;
  private viewMode:string = CatalogCategoryComponent.CATEGORIES;

  private categories:Array<CategoryVO> = [];
  private categoryFilter:string;
  private categoryFilterRequired:boolean = true;
  private categoryFilterCapped:boolean = false;

  delayedFiltering:Future;
  delayedFilteringMs:number = Config.UI_INPUT_DELAY;
  filterCap:number = Config.UI_FILTER_CAP;
  filterNoCap:number = Config.UI_FILTER_NO_CAP;

  private selectedCategory:CategoryVO;

  private categoryEdit:CategoryVO;
  private categoryEditAttributes:AttrValueCategoryVO[] = [];
  private categoryAttributesUpdate:Array<Pair<AttrValueCategoryVO, boolean>>;

  @ViewChild('deleteConfirmationModalDialog')
  deleteConfirmationModalDialog:ModalComponent;

  private deleteValue:String;

  @ViewChild('categorySelectComponent')
  categorySelectComponent:CategorySelectComponent;

  constructor(private _categoryService:CatalogService) {
    console.debug('CatalogCategoryComponent constructed');
  }

  changed:boolean = false;
  validForSave:boolean = false;

  newCategoryInstance():CategoryVO {
    return {
      categoryId: 0,
      parentId: 0, parentName: null,
      linkToId: 0, linkToName: null,
      rank: 500,
      productTypeId: 0, productTypeName: null,
      name: '', guid: null, displayNames: [], description: null,
      uitemplate: null,
      availablefrom: null, availableto: null,
      uri: null, title: null, metakeywords: null, metadescription: null, displayTitles: [], displayMetakeywords: [], displayMetadescriptions: [],
      navigationByAttributes: false, navigationByBrand: false, navigationByPrice: false, navigationByPriceTiers: { tiers: [] }, children: []
    };
  }

  ngOnInit() {
    console.debug('CatalogCategoryComponent ngOnInit');
    this.onRefreshHandler();
    let that = this;
    this.delayedFiltering = Futures.perpetual(function() {
      that.getFilteredCategories();
    }, this.delayedFilteringMs);

  }

  ngOnDestroy() {
    console.debug('CatalogCategoryComponent ngOnDestroy');
  }


  onFilterChange(event:any) {

    this.delayedFiltering.delay();

  }

  getFilteredCategories() {
    this.categoryFilterRequired = !this.forceShowAll && (this.categoryFilter == null || this.categoryFilter.length < 2);

    console.debug('CatalogCategoryComponent getFilteredCategories' + (this.forceShowAll ? ' forcefully': ''));

    if (!this.categoryFilterRequired) {
      let max = this.forceShowAll ? this.filterNoCap : this.filterCap;
      var _sub:any = this._categoryService.getFilteredCategories(this.categoryFilter, max).subscribe( allcategories => {
        console.debug('CatalogCategoryComponent getAllCountries', allcategories);
        this.categories = allcategories;
        this.selectedCategory = null;
        this.categoryEdit = null;
        this.viewMode = CatalogCategoryComponent.CATEGORIES;
        this.changed = false;
        this.validForSave = false;
        this.categoryFilterCapped = this.categories.length >= max;
        _sub.unsubscribe();
      });
    } else {
      this.categories = [];
      this.selectedCategory = null;
      this.categoryEdit = null;
      this.categoryEditAttributes = null;
      this.viewMode = CatalogCategoryComponent.CATEGORIES;
      this.changed = false;
      this.validForSave = false;
      this.categoryFilterCapped = false;
    }
  }

  protected onRefreshHandler() {
    console.debug('CatalogCategoryComponent refresh handler');
    this.getFilteredCategories();
  }

  onCategorySelected(data:CategoryVO) {
    console.debug('CatalogCategoryComponent onCategorySelected', data);
    this.selectedCategory = data;
  }

  onCategoryChanged(event:FormValidationEvent<Pair<CategoryVO, Array<Pair<AttrValueCategoryVO, boolean>>>>) {
    console.debug('CatalogCategoryComponent onCategoryChanged', event);
    this.changed = true;
    this.validForSave = event.valid;
    this.categoryEdit = event.source.first;
    this.categoryAttributesUpdate = event.source.second;
  }

  protected onForceShowAll() {
    this.forceShowAll = !this.forceShowAll;
    this.getFilteredCategories();
  }

  protected onBackToList() {
    console.debug('CatalogCategoryComponent onBackToList handler');
    if (this.viewMode === CatalogCategoryComponent.CATEGORY) {
      this.categoryEdit = null;
      this.viewMode = CatalogCategoryComponent.CATEGORIES;
    }
  }

  protected onViewTree() {
    console.debug('CatalogCategoryComponent onViewTree handler');
    this.categorySelectComponent.showDialog();
  }

  protected onCatalogTreeDataSelected(event:FormValidationEvent<CategoryVO>) {
    console.debug('CatalogCategoryComponent onCatalogTreeDataSelected handler', event);
    if (event.valid) {
      this.categoryFilter = event.source.guid;
      this.getFilteredCategories();
    }
  }

  protected onRowNew() {
    console.debug('CatalogCategoryComponent onRowNew handler');
    this.changed = false;
    this.validForSave = false;
    if (this.viewMode === CatalogCategoryComponent.CATEGORIES) {
      this.categoryEdit = this.newCategoryInstance();
      this.categoryEditAttributes = [];
      this.viewMode = CatalogCategoryComponent.CATEGORY;
    }
  }

  protected onRowDelete(row:any) {
    console.debug('CatalogCategoryComponent onRowDelete handler', row);
    this.deleteValue = row.name;
    this.deleteConfirmationModalDialog.show();
  }

  protected onRowDeleteSelected() {
    if (this.selectedCategory != null) {
      this.onRowDelete(this.selectedCategory);
    }
  }


  protected onRowEditCategory(row:CategoryVO) {
    console.debug('CatalogCategoryComponent onRowEditCategory handler', row);
    this.categoryEdit = Util.clone(row);
    this.categoryEditAttributes = [];
    this.changed = false;
    this.validForSave = false;
    this.viewMode = CatalogCategoryComponent.CATEGORY;
    if (this.categoryEdit.categoryId > 0) {
      var _sub:any = this._categoryService.getCategoryAttributes(this.categoryEdit.categoryId).subscribe(attrs => {
        this.categoryEditAttributes = attrs;
        _sub.unsubscribe();
      });
    }
  }

  protected onRowEditSelected() {
    if (this.selectedCategory != null) {
      this.onRowEditCategory(this.selectedCategory);
    }
  }


  protected onRowLinkSelected() {
    console.debug('CatalogCategoryComponent onRowLinkSelected handler');
    this.changed = false;
    this.validForSave = false;
    if (this.viewMode === CatalogCategoryComponent.CATEGORIES) {
      this.categoryEdit = this.newCategoryInstance();
      this.categoryEdit.linkToId = this.selectedCategory.categoryId;
      this.categoryEdit.linkToName = this.selectedCategory.name;
      this.categoryEditAttributes = [];
      this.viewMode = CatalogCategoryComponent.CATEGORY;
    }
  }

  protected onSaveHandler() {

    if (this.validForSave && this.changed) {

      if (this.categoryEdit != null) {

        console.debug('CatalogCategoryComponent Save handler category', this.categoryEdit);

        var _sub:any = this._categoryService.saveCategory(this.categoryEdit).subscribe(
            rez => {
              _sub.unsubscribe();
              let pk = this.categoryEdit.categoryId;
              console.debug('CatalogCategoryComponent category changed', rez);
              this.categoryFilter = rez.guid;
              this.changed = false;
              this.selectedCategory = rez;
              this.categoryEdit = null;
              this.viewMode = CatalogCategoryComponent.CATEGORIES;

              if (pk > 0 && this.categoryAttributesUpdate != null && this.categoryAttributesUpdate.length > 0) {

                var _sub2:any = this._categoryService.saveCategoryAttributes(this.categoryAttributesUpdate).subscribe(rez => {
                  _sub2.unsubscribe();
                  console.debug('CatalogCategoryComponent category attributes updated', rez);
                  this.categoryAttributesUpdate = null;
                  this.getFilteredCategories();
                });
              } else {
                this.getFilteredCategories();
              }
          }
        );
      }

    }

  }

  protected onDiscardEventHandler() {
    console.debug('CatalogCategoryComponent discard handler');
    if (this.viewMode === CatalogCategoryComponent.CATEGORY) {
      if (this.selectedCategory != null) {
        this.onRowEditSelected();
      } else {
        this.onRowNew();
      }
    }
  }

  protected onDeleteConfirmationResult(modalresult: ModalResult) {
    console.debug('CatalogCategoryComponent onDeleteConfirmationResult modal result is ', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {

      if (this.selectedCategory != null) {
        console.debug('CatalogCategoryComponent onDeleteConfirmationResult', this.selectedCategory);

        var _sub:any = this._categoryService.removeCategory(this.selectedCategory).subscribe(res => {
          _sub.unsubscribe();
          console.debug('CatalogCategoryComponent removeCategory', this.selectedCategory);
          this.selectedCategory = null;
          this.categoryEdit = null;
          this.getFilteredCategories();
        });
      }
    }
  }

}
