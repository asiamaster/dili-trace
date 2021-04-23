class NewRegisterBillAdd extends WebConfig {
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
        let registerHeadController = new RegisterHeadController();
        let cityController = new CityController();
        super.initTraceAutoComplete($("[name='originInput']"), function (query, done) {
            cityController.lookupCities(query, done);
        }, function (suggestion) {
            $(this).val(suggestion.value);
            $('[name="originId"]').val(suggestion.id);
            $('[name="originName"]').val(suggestion.value);
            $(this).valid();
        });
        let registerHeadCode = this.form.find('select[name="registerHeadCode"]');
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
            let userId = suggestion.id;
            $('[name="userId"]').val(userId);
            $('[name="name"]').val(suggestion.item.name);
            $(this).valid();
            arrivalTallynosSelect2.html('');
            (async () => {
                try {
                    arrivalTallynosSelect2.val(null).trigger('change');
                    let dataList = await customerController.listSellerTallaryNoByUserId(userId);
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
            registerHeadCode.html('');
            if (30 == $('#registType').val()) {
                (async () => {
                    registerHeadCode.val(null).trigger('change');
                    let dataList = await registerHeadController.listRegisterHead({ userId: userId, minRemainWeight: 0 });
                    $.each(dataList, async (i, v) => {
                        var registerHeadCode = v['code'];
                        var newOption = new Option(v['productName'], registerHeadCode, false, false);
                        registerHeadCode.append(newOption).trigger('change');
                    });
                })();
            }
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
        var registerHeadCodeInput = $('select[name="registerHeadCode"]');
        $('#registType').on('change', async (e) => {
            if (30 != $(e.target).val()) {
                $('#registerHeadDiv').hide();
                $('[name="registerHeadCode"]').val('');
                this.form.find('input[type="text"]').prop('readonly', false);
                return;
            }
            $('#registerHeadDiv').show();
            this.form.find('input[type="text"]').prop('readonly', true);
            this.form.find('input[name="plate"]').prop('readonly', false);
            $('#userInputDiv').find('input').prop('readonly', false);
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
