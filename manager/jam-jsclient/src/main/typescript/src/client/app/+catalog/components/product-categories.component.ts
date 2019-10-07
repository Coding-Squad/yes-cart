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
import { Component, OnInit, OnDestroy, Input, ViewChild, Output, EventEmitter } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { YcValidators } from './../../shared/validation/validators';
import { CatalogService } from './../../shared/services/index';
import { ProductVO, ProductCategoryVO, CategoryVO, BasicCategoryVO, ValidationRequestVO } from './../../shared/model/index';
import { ITreeNode } from './../../shared/tree-view/index';
import { ModalComponent, ModalResult, ModalAction } from './../../shared/modal/index';
import { UiUtil } from './../../shared/ui/index';
import { LogUtil } from './../../shared/log/index';
import { Util } from './../../shared/services/util';

@Component({
  selector: 'yc-product-categories',
  moduleId: module.id,
  templateUrl: 'product-categories.component.html',
})

/**
 * Manage categories assigned to product.
 */
export class ProductCategoryComponent implements OnInit, OnDestroy {

  private static _categories:Array<CategoryVO> = null;

  @Output() dataChanged: EventEmitter<Array<ProductCategoryVO>> = new EventEmitter<Array<ProductCategoryVO>>();

  private _product:ProductVO;
  private _reload:boolean = false;

  private changed:boolean = false;
  private existingProduct:boolean = false;

  private newCategory:BasicCategoryVO;
  @ViewChild('editNewCategoryName')
  private editNewCategoryName:ModalComponent;

  private editCategory:ProductCategoryVO;
  @ViewChild('editCategoryRank')
  private editCategoryRank:ModalComponent;

  private newCategoryForm:any;
  private validForSave:boolean = false;
  private editCategoryForm:any;
  private validForEdit:boolean = false;

  private nodes:Array<ITreeNode>;
  private selectedNode:ITreeNode;

  private loading:boolean = false;

  private assigned:Array<ProductCategoryVO> = null;

  /**
   * Construct product catalogues panel.
   * @param _categoryService category service
   * @param fb form builder
   */
  constructor(private _categoryService:CatalogService,
              fb: FormBuilder) {
    LogUtil.debug('ProductCategoryComponent constructed');

    this.newCategory = this.newCategoryInstance();
    this.editCategory = this.newProductCategoryInstance();

    let that = this;

    let validCode = function(control:any):any {

      let code = control.value;
      if (code == null || code == '' || that.newCategory == null || !that.newCategoryForm || !that.newCategoryForm.dirty) {
        return null;
      }

      let basic = YcValidators.validCode(control);
      if (basic == null) {
        let req:ValidationRequestVO = { subject: 'category', subjectId: 0, field: 'guid', value: code };
        return YcValidators.validRemoteCheck(control, req);
      }
      return basic;
    };


    this.newCategoryForm = fb.group({
      'guid': ['', validCode],
      'name': ['', YcValidators.requiredNonBlankTrimmed],
    });

    this.editCategoryForm = fb.group({
      'rank': ['', YcValidators.requiredPositiveWholeNumber],
    });
  }

  @Input()
  set reload(reload:boolean) {
    if (reload && !this._reload) {
      this._reload = true;
      this.loadData();
    }
  }

  @Input()
  set product(product:ProductVO) {
    this._product = product;
    if (this._reload || this.assigned != null) {
      this.loadData();
    }
  }

  get product():ProductVO  {
    return this._product;
  }

  get categories():Array<CategoryVO> {
    return ProductCategoryComponent._categories;
  }

  set categories(categories:Array<CategoryVO>) {
    ProductCategoryComponent._categories = categories;
  }

  ngOnInit() {
    LogUtil.debug('ProductCategoryComponent ngOnInit product', this.product);
    this.formBindAdd();
    this.formBindEdit();
  }

  ngOnDestroy() {
    LogUtil.debug('ProductCategoryComponent ngOnDestroy');
    this.formUnbindAdd();
    this.formUnbindEdit();
  }

  newCategoryInstance():BasicCategoryVO {
    return { name: '', guid: '' };
  }

  newProductCategoryInstance():ProductCategoryVO {
    return { productCategoryId : 0, productId : 0, categoryId : 0, categoryCode : '', categoryName : '', rank : 0 };
  }

  formBindAdd():void {
    UiUtil.formBind(this, 'newCategoryForm', 'formChangeAdd', false);
  }

  formUnbindAdd():void {
    UiUtil.formUnbind(this, 'newCategoryForm');
  }

  formChangeAdd():void {
    LogUtil.debug('ProductCategoryComponent formChangeAdd', this.newCategoryForm.valid, this.newCategory);
    this.validForSave = this.newCategoryForm.valid;
  }


  formBindEdit():void {
    UiUtil.formBind(this, 'editCategoryForm', 'formChangeEdit', false);
  }


  formUnbindEdit():void {
    UiUtil.formUnbind(this, 'editCategoryForm');
  }


  formChangeEdit():void {
    LogUtil.debug('ProductCategoryComponent formChangeEdit', this.editCategoryForm.valid, this.editCategory);
    this.validForEdit = this.editCategoryForm.valid;
  }

  /**
   * Load data and adapt time.
   */
  loadData() {
    LogUtil.debug('ProductCategoryComponent loading categories', this.product);
    this.existingProduct = this.product != null;
    if (this.existingProduct) {

        this.loading = true;

        this.assigned = this._product.productCategories;
        let _assignedIds:Array<number> = this.adaptToIds(this.assigned);

        let _subc:any = this._categoryService.getAllCategories().subscribe(
            cats => {
              LogUtil.debug('ProductCategoryComponent all categories', cats, _assignedIds);
              this.categories = cats;
              this.nodes = this.adaptToTree(cats, _assignedIds);
              this.selectedNode = null;
              UiUtil.formInitialise(this, 'newCategoryForm', 'newCategory', this.newCategoryInstance());
              UiUtil.formInitialise(this, 'editCategoryForm', 'editCategory', this.newProductCategoryInstance());
              this.changed = false;
              this._reload = false;
              this.loading = false;
              _subc.unsubscribe();
          }
        );
    }
  }

  adaptToIds(vo:Array<ProductCategoryVO>):Array<number> {
    let rez:Array<number> = [];
    for (let idx = 0; idx < vo.length; idx++) {
      let catVo:ProductCategoryVO = vo[idx];
      rez.push(catVo.categoryId);
    }
    return rez;
  }

  /**
   * Adapt given list of categories to tree items for representation.
   * @param vo branch
   * @param disabled nodes
   * @returns {Array<ITreeNode>}
     */
  adaptToTree(vo:Array<CategoryVO>, disabled:Array<number>):Array<ITreeNode> {
    let rez:Array<ITreeNode> = [];
    for (let idx = 0; idx < vo.length; idx++) {
      let catVo:CategoryVO = vo[idx];
      let node:ITreeNode = {
        'id': catVo.categoryId.toString(),
        'name': catVo.name,
        'children': [],
        'childrenLoaded': true,
        'expanded': catVo.categoryId === 100, //the root is expanded by default
        'selected': catVo.categoryId === 100, //treat root as already selected
        'disabled': disabled.indexOf(catVo.categoryId) !== -1,
        'source': catVo
      };
      if (catVo.children !== null && catVo.children.length > 0) {
        node.children = this.adaptToTree(catVo.children, disabled);
        node.children.forEach(child => {
          if (child.disabled || child.expanded) {
            node.expanded = true; // Expand parent if child is selected or expanded
          }
        });
      }
      rez.push(node);
    }
    return rez;
  }

  /**
   * Assign selected category to product.
   * @param node
   */
  assignToProductClick(node:ITreeNode) {
    LogUtil.debug('ProductCategoryComponent assignToProduct ', node);
    let catVo = node.source;
    this.assigned.push({ productCategoryId: 0,  productId: this._product.productId, categoryId: catVo.categoryId, categoryCode: catVo.guid, categoryName: catVo.name, rank: 0 });
    LogUtil.debug('ProductCategoryComponent disabled node', node);
    this.changeDisabledState(catVo, this.nodes, true);
    this.selectedNode = null;
    this.changed = true;
    this.dataChanged.emit(this.assigned);
  }

  /**
   * Un-assign from product.
   * @param cat category
     */
  onAssignedClick(cat:CategoryVO) {
    LogUtil.debug('ProductCategoryComponent onAssigned', cat);
    for (let idx = 0; idx < this.assigned.length; idx++) {
      let catVo : ProductCategoryVO = this.assigned[idx];
      if (catVo.categoryId === cat.categoryId) {
        LogUtil.debug('ProductCategoryComponent remove from assigned', catVo);
        this.assigned.splice(idx, 1);
        this.changeDisabledState(catVo, this.nodes, false);
        this.changed = true;
        this.dataChanged.emit(this.assigned);
        break;
      }
    }
  }

  changeDisabledState(cat:ProductCategoryVO, nodes:Array<ITreeNode>, disabled:boolean):boolean {
    let changed = false;
    for (let idx = 0; idx < nodes.length; idx++) {
      let node:ITreeNode = nodes[idx];
      if (node.id === cat.categoryId.toString()) {
        node.disabled = disabled;
        if (disabled) {
          node.expanded = false;
        }
        LogUtil.debug('ProductCategoryComponent enabled node', node);
        changed = true;
      } else if (node.children && this.changeDisabledState(cat, node.children, disabled)) {
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Select node.
   * @param node
   */
  onSelectNode(node:ITreeNode) {
    LogUtil.debug('ProductCategoryComponent selected node', node);
    if (node.disabled === false) {
      node.expanded = false; // collapse on selection, to prevent recursive selection (i.e. sub categories from same branch)
      this.selectedNode = node;
    }
  }

  onRequest(parent:ITreeNode) {
    LogUtil.debug('ProductCategoryComponent onRequest node', parent);
    parent.expanded = !parent.expanded;
  }


  /**
   * Fast create new category.
   * @param parent parent of new catecory
   */
  createNew(parent:ITreeNode) {
    LogUtil.debug('ProductCategoryComponent createNew for parent', parent);
    this.validForSave = false;
    UiUtil.formInitialise(this, 'newCategoryForm', 'newCategory', this.newCategoryInstance());
    this.editNewCategoryName.show();
  }

  /**
   * Handle result of new category modal dialog.
   * @param modalresult
     */
  editNewCategoryNameModalResult(modalresult:ModalResult) {
    LogUtil.debug('ProductCategoryComponent editNewCategoryNameModalResult modal result', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {
      this._categoryService.createCategory(this.newCategory, +this.selectedNode.id).subscribe(
        catVo => {
          this.validForSave = false;
          this.loadData();
        }
      );

    }
  }


  onRankClick(cat:ProductCategoryVO) {
    LogUtil.debug('ProductCategoryComponent onRank', cat);

    this.validForEdit = false;
    UiUtil.formInitialise(this, 'editCategoryForm', 'editCategory', Util.clone(cat));
    this.editCategoryRank.show();

  }


  /**
   * Handle result of new category modal dialog.
   * @param modalresult
   */
  editCategoryRankModalResult(modalresult:ModalResult) {
    LogUtil.debug('ProductCategoryComponent editCategoryRankModalResult modal result', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {

      let _cat = this.assigned.find( cat => {
         return cat.categoryId == this.editCategory.categoryId;
      });

      if (_cat != null) {
        _cat.rank = this.editCategory.rank;
        this.changed = true;
        this.dataChanged.emit(this.assigned);
      }

    }
  }

}
