class NewRegisterBillEdit extends WebConfig {
    constructor(form, submitBtn) {
        super();
        this.form = form;
        this.submitBtn = submitBtn;
        this.submitBtn.on('click', async () => this.doAdd());
        let categoryController = new CategoryController();
        super.initTraceAutoComplete($("[name='categoryInput']"), function (query, done) {
            categoryController.lookupCategories(query, done);
        }, function (suggestion) {
            $(this).val(suggestion.value);
            $('[name="productId"]').val(suggestion.id);
            $('[name="productName"]').val(suggestion.value);
            $(this).valid();
        });
        let cityController = new CityController();
        super.initTraceAutoComplete($("[name='originInput']"), function (query, done) {
            cityController.lookupCities(query, done);
        }, function (suggestion) {
            $(this).val(suggestion.value);
            $('[name="originId"]').val(suggestion.id);
            $('[name="originName"]').val(suggestion.value);
            $(this).valid();
        });
        let arrivalTallynosSelect2 = this.form.find('select[name="arrivalTallynos"][multiple]').select2({
            placeholder: '-- 请选择 --',
            language: "zh-CN",
        });
        let customerController = new CustomerController();
        super.initTraceAutoComplete($("[name='userInput']"), function (query, done) {
            customerController.lookupSeller(query, done);
        }, function (suggestion) {
            $(this).data('select-text', suggestion.value);
            $(this).val(suggestion.value);
            $('[name="userId"]').val(suggestion.id);
            $('[name="name"]').val(suggestion.item.name);
            $(this).valid();
            (async () => {
                try {
                    arrivalTallynosSelect2.html('');
                    arrivalTallynosSelect2.val(null).trigger('change');
                    let dataList = await customerController.listSellerTallaryNoByUserId(suggestion.id);
                    $.each(dataList, async (i, v) => {
                        var tallaryNo = v['assetsName'];
                        var newOption = new Option(tallaryNo, tallaryNo, false, false);
                        arrivalTallynosSelect2.append(newOption).trigger('change');
                    });
                }
                catch (e) {
                    console.error(e);
                }
            })();
        });
        let upstreamController = new UpStreamController();
        super.initTraceAutoComplete($("[name='upStreamName']"), function (query, done) {
            let userId = $('#userId').val();
            upstreamController.lookupUpStreams(query, userId, done);
        }, function (suggestion) {
            $(this).val(suggestion.value);
            $('[name="upStreamId"]').val(suggestion.id);
            $(this).valid();
        });
        let brandController = new BrandController();
        super.initTraceAutoComplete($("[name='brandName']"), function (query, done) {
            brandController.lookupBrands(query, done);
        }, function (suggestion) {
            $(this).val(suggestion.value);
            $('[name="upStreamId"]').val(suggestion.id);
            $(this).valid();
        });
        this.initRegistType();
    }
    initRegistType() {
        var registerHeadCodeInput = $('input[name="registerHeadCodeInput"]');
        let registerHeadController = new RegisterHeadController();
        super.initTraceAutoComplete(registerHeadCodeInput, function (query, done) {
            $.extend(query, { userId: $('input[name="userId"]').val() });
            registerHeadController.lookupRegisterHead(query, done);
        }, async (suggestion, a, b) => {
            debugger;
            $(this).val(suggestion.value);
            let registerHeadCode = suggestion.item.code;
            $('[name="registerHeadCode"]').val(registerHeadCode);
            let plateList = suggestion.item.plateList;
            let arrivalTallynos = suggestion.item.arrivalTallynos;
            debugger;
            $(this).valid();
        });
        $('#registType').on('change', async (e) => {
            if (30 != $(e.target).val()) {
                registerHeadCodeInput.parent('div').hide();
                return;
            }
            registerHeadCodeInput.parent('div').show();
        });
    }
    async doAdd() {
        bs4pop.removeAll();
        let url = super.toUrl("/newRegisterBill/doAdd.action");
        if (!this.form.validate().form()) {
            bs4pop.notice("请完善必填项", { type: 'warning', position: 'topleft' });
            return;
        }
        let arr = [];
        let imageCertList = Object.values(imageCertListTemp).reduce((arr, item) => arr.concat(item));
        let registerBill = super.serializeJSON(this.form, {
            customTypes: {
                hmcustFun: function (strVal, el) {
                    if ($.trim(strVal) != '') {
                        return strVal + ':00';
                    }
                    return strVal;
                }
            }
        });
        registerBill['imageCertList'] = imageCertList;
        debugger;
        return;
        try {
            let resp = await jq.postJsonWithProcessing(url, { registerBills: [registerBill] });
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            bs4pop.removeAll();
            bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
        }
        catch (e) {
            bs4pop.alert('远程访问失败', { type: 'error' });
        }
    }
}
