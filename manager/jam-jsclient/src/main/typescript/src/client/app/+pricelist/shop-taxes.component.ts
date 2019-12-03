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
import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { YcValidators } from './../shared/validation/validators';
import { ShopEventBus, PricingService, UserEventBus, Util } from './../shared/services/index';
import { PromotionTestConfigComponent } from './components/index';
import { ModalComponent, ModalResult, ModalAction } from './../shared/modal/index';
import { TaxVO, ShopVO, TaxConfigVO, PromotionTestVO, CartVO } from './../shared/model/index';
import { Futures, Future } from './../shared/event/index';
import { Config } from './../shared/config/env.config';
import { UiUtil } from './../shared/ui/index';
import { LogUtil } from './../shared/log/index';
import { CookieUtil } from './../shared/cookies/index';

@Component({
  selector: 'yc-shop-taxes',
  moduleId: module.id,
  templateUrl: 'shop-taxes.component.html',
})

export class ShopTaxesComponent implements OnInit, OnDestroy {

  private static COOKIE_SHOP:string = 'ADM_UI_TAX_SHOP';
  private static COOKIE_CURRENCY:string = 'ADM_UI_TAX_CURR';

  private static _selectedShop:ShopVO;
  private static _selectedCurrency:string;

  private static TAXES:string = 'taxes';
  private static CONFIGS:string = 'taxconfigs';
  private static PRICELIST_TEST:string = 'pricelisttest';

  private searchHelpTaxShow:boolean = false;
  private searchHelpTaxConfigShow:boolean = false;
  private forceShowAll:boolean = false;
  private viewMode:string = ShopTaxesComponent.TAXES;

  private taxes:Array<TaxVO> = [];
  private taxesFilter:string;
  private taxesFilterRequired:boolean = true;
  private taxesFilterCapped:boolean = false;

  private taxconfigs:Array<TaxConfigVO> = [];
  private taxconfigsFilter:string;
  private taxconfigsFilterRequired:boolean = true;
  private taxconfigsFilterCapped:boolean = false;

  private delayedFilteringTax:Future;
  private delayedFilteringTaxConfig:Future;
  private delayedFilteringMs:number = Config.UI_INPUT_DELAY;
  private filterCap:number = Config.UI_FILTER_CAP;
  private filterNoCap:number = Config.UI_FILTER_NO_CAP;

  private selectedTax:TaxVO;
  private selectedTaxconfig:TaxConfigVO;

  private taxEdit:TaxVO;
  private taxEditForm:any;
  private validForSaveTax:boolean = false;

  private taxconfigEdit:TaxConfigVO;
  private taxconfigEditForm:any;
  private validForSaveTaxconfig:boolean = true;

  @ViewChild('deleteConfirmationModalDialog')
  private deleteConfirmationModalDialog:ModalComponent;

  @ViewChild('editTaxModalDialog')
  private editTaxModalDialog:ModalComponent;

  @ViewChild('editTaxconfigModalDialog')
  private editTaxconfigModalDialog:ModalComponent;

  @ViewChild('selectShopModalDialog')
  private selectShopModalDialog:ModalComponent;

  @ViewChild('selectCurrencyModalDialog')
  private selectCurrencyModalDialog:ModalComponent;

  @ViewChild('runTestModalDialog')
  private runTestModalDialog:PromotionTestConfigComponent;

  private deleteValue:String;

  private loading:boolean = false;

  private testCart:CartVO;

  private userSub:any;

  constructor(private _taxService:PricingService,
              fb: FormBuilder) {
    LogUtil.debug('ShopTaxesComponent constructed');

    this.taxEditForm = fb.group({
      'code': ['', YcValidators.requiredValidCode],
      'description': ['', YcValidators.requiredNonBlankTrimmed],
      'exclusiveOfPrice': [''],
      'taxRate': ['', YcValidators.requiredPositiveNumber],
      'shopCode': ['', Validators.required],
      'currency': ['', Validators.required],
    });

    this.taxconfigEditForm = fb.group({
       'productCode': ['', YcValidators.validCode],
       'stateCode': ['', YcValidators.validCode],
       'countryCode': ['', YcValidators.validCountryCode],
    });
  }

  get selectedShop():ShopVO {
     return ShopTaxesComponent._selectedShop;
  }

  set selectedShop(selectedShop:ShopVO) {
    ShopTaxesComponent._selectedShop = selectedShop;
  }

  get selectedCurrency():string {
     return ShopTaxesComponent._selectedCurrency;
  }

  set selectedCurrency(selectedCurrency:string) {
    ShopTaxesComponent._selectedCurrency = selectedCurrency;
  }

  newTaxInstance():TaxVO {
    return { taxId: 0, taxRate: 0, exclusiveOfPrice: false, shopCode: this.selectedShop.code, currency: this.selectedCurrency, code: '', guid: null, description: null};
  }

  newTaxConfigInstance():TaxConfigVO {
    return { taxConfigId: 0, taxId: this.selectedTax.taxId, productCode: null, stateCode: null, countryCode: null, guid: null};
  }

  ngOnInit() {
    LogUtil.debug('ShopTaxesComponent ngOnInit');

    this.onRefreshHandler();

    this.userSub = UserEventBus.getUserEventBus().userUpdated$.subscribe(user => {
      this.presetFromCookie();
    });

    let that = this;
    this.delayedFilteringTax = Futures.perpetual(function() {
      that.getFilteredTax();
    }, this.delayedFilteringMs);
    this.delayedFilteringTaxConfig = Futures.perpetual(function() {
      that.getFilteredTaxConfig();
    }, this.delayedFilteringMs);
    this.formBind();
  }

  ngOnDestroy() {
    LogUtil.debug('ShopTaxesComponent ngOnDestroy');
    this.formUnbind();
    if (this.userSub) {
      this.userSub.unsubscribe();
    }
  }

  formBind():void {
    UiUtil.formBind(this, 'taxEditForm', 'formChangeTax', false);
    UiUtil.formBind(this, 'taxconfigEditForm', 'formChangeTaxconfig', false);
  }

  formUnbind():void {
    UiUtil.formUnbind(this, 'taxEditForm');
    UiUtil.formUnbind(this, 'taxconfigEditForm');
  }


  formChangeTax():void {
    LogUtil.debug('ShopTaxesComponent formChangeTax', this.taxEditForm.valid, this.taxEdit);
    this.validForSaveTax = this.taxEditForm.valid;
  }


  formChangeTaxconfig():void {
    LogUtil.debug('ShopTaxesComponent formChangeTaxconfig', this.taxconfigEditForm.valid, this.taxconfigEdit);
    this.validForSaveTaxconfig = this.taxconfigEditForm.valid;
  }


  protected presetFromCookie() {

    if (this.selectedShop == null) {
      let shopCode = CookieUtil.readCookie(ShopTaxesComponent.COOKIE_SHOP, null);
      if (shopCode != null) {
        let shops = ShopEventBus.getShopEventBus().currentAll();
        if (shops != null) {
          shops.forEach(shop => {
            if (shop.code == shopCode) {
              this.selectedShop = shop;
              LogUtil.debug('ShopTaxesComponent ngOnInit presetting shop from cookie', shop);
            }
          });
        }
      }
    }
    if (this.selectedCurrency == null) {
      let curr = CookieUtil.readCookie(ShopTaxesComponent.COOKIE_CURRENCY, null);
      if (curr != null) {
        this.selectedCurrency = curr;
        LogUtil.debug('ShopTaxesComponent ngOnInit presetting currency from cookie', curr);
      }
    }

  }


  protected onShopSelect() {
    LogUtil.debug('ShopTaxesComponent onShopSelect');
    this.selectShopModalDialog.show();
  }

  protected onShopSelected(event:ShopVO) {
    LogUtil.debug('ShopTaxesComponent onShopSelected');
    this.selectedShop = event;
    if (this.selectedShop != null) {
      CookieUtil.createCookie(ShopTaxesComponent.COOKIE_SHOP, this.selectedShop.code, 360);
    }
  }

  protected onSelectShopResult(modalresult: ModalResult) {
    LogUtil.debug('ShopTaxesComponent onSelectShopResult modal result is ', modalresult);
    if (this.selectedShop == null) {
      this.selectShopModalDialog.show();
    } else if (this.selectedCurrency == null) {
      this.selectCurrencyModalDialog.show();
    } else {
      this.getFilteredTax();
    }
  }

  protected onCurrencySelect() {
    LogUtil.debug('ShopTaxesComponent onCurrencySelect');
    this.selectCurrencyModalDialog.show();
  }

  protected onCurrencySelected(event:string) {
    LogUtil.debug('ShopTaxesComponent onCurrencySelected');
    this.selectedCurrency = event;
    if (this.selectedCurrency != null) {
      CookieUtil.createCookie(ShopTaxesComponent.COOKIE_CURRENCY, this.selectedCurrency, 360);
    }
  }

  protected onSelectCurrencyResult(modalresult: ModalResult) {
    LogUtil.debug('ShopTaxesComponent onSelectCurrencyResult modal result is ', modalresult);
    if (this.selectedCurrency == null) {
      this.selectCurrencyModalDialog.show();
    } else {
      this.getFilteredTax();
    }
  }

  protected onTestRules() {
    LogUtil.debug('ShopTaxesComponent onTestRules');
    this.runTestModalDialog.showDialog();
  }

  onRunTestResult(event:PromotionTestVO) {
    LogUtil.debug('ShopTaxesComponent onRunTestResult', event);
    if (event != null) {
      this.loading = true;
      let _sub:any = this._taxService.testPromotions(this.selectedShop, this.selectedCurrency, event).subscribe(
        cart => {
          _sub.unsubscribe();
          this.loading = false;
          LogUtil.debug('ShopTaxesComponent onTestRules', cart);
          this.viewMode = ShopTaxesComponent.PRICELIST_TEST;
          this.testCart = cart;
        }
      );

    }
  }

  protected onTaxFilterChange(event:any) {

    this.delayedFilteringTax.delay();

  }

  protected onTaxConfigFilterChange(event:any) {

    this.delayedFilteringTaxConfig.delay();

  }

  protected onRefreshHandler() {
    LogUtil.debug('ShopTaxesComponent refresh handler');
    if (UserEventBus.getUserEventBus().current() != null) {
      this.presetFromCookie();
      if (this.viewMode == ShopTaxesComponent.CONFIGS) {
        this.getFilteredTaxConfig();
      } else {
        this.getFilteredTax();
      }
    }
  }

  protected onTaxSelected(data:TaxVO) {
    LogUtil.debug('ShopTaxesComponent onTaxSelected', data);
    this.selectedTax = data;
    // this.getFilteredTaxConfig();
  }

  protected onTaxconfigSelected(data:TaxConfigVO) {
    LogUtil.debug('ShopTaxesComponent onTaxconfigSelected', data);
    this.selectedTaxconfig = data;
  }

  protected onSearchHelpToggleTax() {
    this.searchHelpTaxShow = !this.searchHelpTaxShow;
  }

  protected onSearchHelpToggleTaxConfig() {
    this.searchHelpTaxConfigShow = !this.searchHelpTaxConfigShow;
  }

  protected onSearchRate() {
    this.taxesFilter = '%';
    this.searchHelpTaxShow = false;
  }

  protected onSearchType() {
    this.taxesFilter = '-';
    this.searchHelpTaxShow = false;
  }

  protected onSearchLocation() {
    this.taxesFilter = '@';
    this.searchHelpTaxConfigShow = false;
  }

  protected onSearchSKU() {
    this.taxesFilter = '#';
    this.searchHelpTaxConfigShow = false;
  }

  protected onSearchSKUExact() {
    this.taxesFilter = '!';
    this.searchHelpTaxConfigShow = false;
  }

  protected onForceShowAll() {
    this.forceShowAll = !this.forceShowAll;
    if (this.viewMode == ShopTaxesComponent.CONFIGS) {
      this.getFilteredTaxConfig();
    }
  }

  protected onRowNew() {
    LogUtil.debug('ShopTaxesComponent onRowNew handler');
    this.validForSaveTax = false;
    if (this.viewMode == ShopTaxesComponent.CONFIGS) {

      UiUtil.formInitialise(this, 'taxconfigEditForm', 'taxconfigEdit', this.newTaxConfigInstance());
      this.editTaxconfigModalDialog.show();

    } else {

      UiUtil.formInitialise(this, 'taxEditForm', 'taxEdit', this.newTaxInstance(), false, ['code']);
      this.editTaxModalDialog.show();

    }
  }

  protected onRowTaxDelete(row:TaxVO) {
    LogUtil.debug('ShopTaxesComponent onRowDelete handler', row);
    this.deleteValue = row.code + (row.description ? ': ' + row.description : '');
    this.deleteConfirmationModalDialog.show();
  }

  protected onRowTaxConfigDelete(row:TaxConfigVO) {
    LogUtil.debug('ShopTaxesComponent onRowTaxConfigDelete handler', row);
    this.deleteValue =
      (row.countryCode ? row.countryCode + '/' : '-') +
      (row.stateCode ? row.stateCode + '/' : '-') +
      (row.productCode ? row.productCode : '-');
    this.deleteConfirmationModalDialog.show();
  }

  protected onRowDeleteSelected() {
    if (this.selectedTaxconfig != null) {
      this.onRowTaxConfigDelete(this.selectedTaxconfig);
    } else if (this.selectedTax != null) {
      this.onRowTaxDelete(this.selectedTax);
    }
  }


  protected onRowEditTax(row:TaxVO) {
    LogUtil.debug('ShopTaxesComponent onRowEditTax handler', row);
    this.validForSaveTax = false;
    UiUtil.formInitialise(this, 'taxEditForm', 'taxEdit', Util.clone(row), row.taxId > 0, ['code']);
    this.editTaxModalDialog.show();
  }

  protected onRowEditTaxconfig(row:TaxConfigVO) {
    LogUtil.debug('ShopTaxesComponent onRowEditTaxconfig handler', row);
    this.validForSaveTaxconfig = true;
    UiUtil.formInitialise(this, 'taxconfigEditForm', 'taxconfigEdit', Util.clone(row));
    this.editTaxconfigModalDialog.show();
  }

  protected onRowEditSelected() {
    if (this.selectedTax != null) {
      this.onRowEditTax(this.selectedTax);
    }
  }

  protected onRowCopySelected() {
    if (this.selectedTaxconfig != null) {
      let copyCfg:TaxConfigVO = Util.clone(this.selectedTaxconfig);
      copyCfg.taxConfigId = 0;
      this.onRowEditTaxconfig(copyCfg);
    } else if (this.selectedTax != null) {
      let copyTax:TaxVO = Util.clone(this.selectedTax);
      copyTax.taxId = 0;
      this.onRowEditTax(copyTax);
    }
  }


  protected onRowList(row:TaxVO) {
    LogUtil.debug('ShopTaxesComponent onRowList handler', row);
    this.viewMode = ShopTaxesComponent.CONFIGS;
    this.getFilteredTaxConfig();
  }


  protected onRowListSelected() {
    if (this.selectedTax != null) {
      this.onRowList(this.selectedTax);
    }
  }


  protected onBackToList() {
    LogUtil.debug('ShopTaxesComponent onBackToList handler');
    if (this.viewMode === ShopTaxesComponent.PRICELIST_TEST) {
      if (this.selectedTax != null) {
        this.viewMode = ShopTaxesComponent.CONFIGS;
      } else {
        this.viewMode = ShopTaxesComponent.TAXES;
      }
    } else if (this.viewMode === ShopTaxesComponent.CONFIGS) {
      this.taxEdit = null;
      this.viewMode = ShopTaxesComponent.TAXES;
    }
  }

  protected onSaveHandler() {

    if (this.taxconfigEdit != null) {

      if (this.validForSaveTaxconfig) {

        LogUtil.debug('ShopTaxesComponent Save handler config', this.taxconfigEdit);

        this.loading = true;
        let _sub:any = this._taxService.createTaxConfig(this.taxconfigEdit).subscribe(
            rez => {
              _sub.unsubscribe();
              let pk = this.taxconfigEdit.taxConfigId;
              LogUtil.debug('ShopTaxesComponent config changed', rez);
              this.selectedTaxconfig = rez;
              if (pk == 0) {
                this.taxconfigsFilter = rez.code;
              }
              this.loading = false;
              this.getFilteredTaxConfig();
          }
        );
      }

    } else if (this.taxEdit != null) {

      if (this.validForSaveTax) {

        LogUtil.debug('ShopTaxesComponent Save handler tax', this.taxEdit);

        this.loading = true;
        let _sub:any = this._taxService.saveTax(this.taxEdit).subscribe(
            rez => {
              _sub.unsubscribe();
              let pk = this.taxEdit.taxId;
              LogUtil.debug('ShopTaxesComponent tax changed', rez);
              this.selectedTax = rez;
              if (pk == 0) {
                this.taxesFilter = rez.code;
              }
              this.loading = false;
              this.getFilteredTax();
          }
        );
      }

    }

  }

  protected onDiscardEventHandler() {
    LogUtil.debug('ShopTaxesComponent discard handler');
    if (this.selectedTax != null) {
      this.onRowEditSelected();
    } else {
      this.onRowNew();
    }
  }

  protected onEditTaxResult(modalresult: ModalResult) {
    LogUtil.debug('ShopTaxesComponent onEditTaxResult modal result is ', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {

      this.onSaveHandler();

    } else {

      if (this.taxconfigEdit != null) {
        this.taxconfigEdit = null;
      } else {
        this.taxEdit = null;
      }

    }
  }

  protected onDeleteConfirmationResult(modalresult: ModalResult) {
    LogUtil.debug('ShopTaxesComponent onDeleteConfirmationResult modal result is ', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {

      if (this.selectedTaxconfig != null) {
        LogUtil.debug('ShopTaxesComponent onDeleteConfirmationResult', this.selectedTaxconfig);

        this.loading = true;
        let _sub:any = this._taxService.removeTaxConfig(this.selectedTaxconfig).subscribe(res => {
          _sub.unsubscribe();
          LogUtil.debug('ShopTaxesComponent removeTax', this.selectedTaxconfig);
          this.selectedTaxconfig = null;
          this.loading = false;
          this.getFilteredTaxConfig();
        });

      } else if (this.selectedTax != null) {
        LogUtil.debug('ShopTaxesComponent onDeleteConfirmationResult', this.selectedTax);

        this.loading = true;
        let _sub:any = this._taxService.removeTax(this.selectedTax).subscribe(res => {
          _sub.unsubscribe();
          LogUtil.debug('ShopTaxesComponent removeTax', this.selectedTax);
          this.selectedTax = null;
          this.loading = false;
          this.getFilteredTax();
        });
      }
    }
  }

  protected onClearFilterTax() {

    this.taxesFilter = '';
    this.delayedFilteringTax.delay();

  }


  protected onClearFilterTaxConfig() {

    this.taxconfigsFilter = '';
    this.delayedFilteringTaxConfig.delay();

  }


  private getFilteredTax() {
    this.taxesFilterRequired = false; // !this.forceShowAll && (this.taxesFilter == null || this.taxesFilter.length < 2);

    LogUtil.debug('ShopTaxesComponent getFilteredTax');

    if (this.selectedShop != null && this.selectedCurrency != null && !this.taxesFilterRequired) {
      this.loading = true;
      let max = this.filterNoCap; // this.forceShowAll ? this.filterNoCap : this.filterCap;
      let _sub:any = this._taxService.getFilteredTax(this.selectedShop, this.selectedCurrency, this.taxesFilter, max).subscribe( alltaxes => {
        LogUtil.debug('ShopTaxesComponent getFilteredTax', alltaxes);
        this.taxes = alltaxes;
        this.selectedTax = null;
        this.validForSaveTax = false;
        this.taxesFilterCapped = this.taxes.length >= max;
        _sub.unsubscribe();
        this.viewMode = ShopTaxesComponent.TAXES;
        this.loading = false;
      });
    } else {
      this.taxes = [];
      this.selectedTax = null;
      this.validForSaveTax = false;
      this.taxesFilterCapped = false;
      this.viewMode = ShopTaxesComponent.TAXES;
    }
  }

  private getFilteredTaxConfig() {
    this.taxconfigsFilterRequired = !this.forceShowAll && (this.taxconfigsFilter == null || this.taxconfigsFilter.length < 2);

    LogUtil.debug('ShopTaxesComponent getFilteredTaxConfig' + (this.forceShowAll ? ' forcefully': ''));

    if (this.selectedTax != null && !this.taxconfigsFilterRequired) {
      this.loading = true;
      let max = this.forceShowAll ? this.filterNoCap : this.filterCap;
      let _sub:any = this._taxService.getFilteredTaxConfig(this.selectedTax, this.taxconfigsFilter, max).subscribe( alltaxes => {
        LogUtil.debug('ShopTaxesComponent getFilteredTaxConfig', alltaxes);
        this.taxconfigs = alltaxes;
        this.selectedTaxconfig = null;
        this.validForSaveTaxconfig = true;
        this.taxconfigsFilterCapped = this.taxconfigs.length >= max;
        this.viewMode = ShopTaxesComponent.CONFIGS;
        _sub.unsubscribe();
        this.loading = false;
      });
    } else {
      this.taxconfigs = [];
      this.selectedTaxconfig = null;
      this.validForSaveTaxconfig = true;
      this.taxconfigsFilterCapped = false;
      this.viewMode = ShopTaxesComponent.CONFIGS;
    }
  }

}
