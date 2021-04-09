class CommissionBillGrid extends WebConfig {
    constructor(grid, queryform, billStateEnums, billDetectStateEnums) {
        super();
        this.grid = grid;
        this.queryform = queryform;
        this.billStateEnums = billStateEnums;
        this.billDetectStateEnums = billDetectStateEnums;
        this.queryform.find('#query').click(async () => await this.queryGridData());
        $('#add-btn').on('click', async () => await this.openCreatePage());
        this.initAutoComplete($("[name='productName']"), '/toll/category.action');
        this.initAutoComplete($("[name='originName']"), '/toll/city.action');
        (async () => {
            await this.queryGridData();
        })();
    }
    openCreatePage() {
        let url = this.toUrl("/commissionBill/create.html");
        var cthis = this;
        var dia = bs4pop.dialog({
            title: '新增委托单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    async queryGridData() {
        if (!this.queryform.validate().form()) {
            bs4pop.notice("请完善必填项", { type: 'warning', position: 'topleft' });
            return;
        }
        await this.remoteQuery();
    }
    async remoteQuery() {
        $('#toolbar button').attr('disabled', "disabled");
        this.grid.bootstrapTable('showLoading');
        this.highLightBill = await this.findHighLightBill();
        try {
            let url = this.toUrl("/commissionBill/listPage.action");
            let resp = await jq.postJson(url, this.queryform.serializeJSON(), {});
            this.grid.bootstrapTable('load', resp);
        }
        catch (e) {
            console.error(e);
            this.grid.bootstrapTable('load', { rows: [], total: 0 });
        }
        this.grid.bootstrapTable('hideLoading');
        $('#toolbar button').removeAttr('disabled');
    }
    async findHighLightBill() {
        try {
            var url = this.toUrl("/commissionBill/findHighLightBill.action");
            return await jq.postJson(url, {}, {});
        }
        catch (e) {
            console.log(e);
            return {};
        }
    }
    get rows() {
        return this.grid.datagrid("getSelections");
    }
    findReviewCheckData() {
        var detectStateArr = [this.billDetectStateEnums.NO_PASS, this.billDetectStateEnums.REVIEW_NO_PASS];
        var alreadyCheckArray = this.filterByProp('$_state', this.billStateEnums.ALREADY_CHECK);
        let values = _.chain(alreadyCheckArray).filter(element => $.inArray(element['$_detectState'], detectStateArr) > -1).value();
        return values;
    }
    isReviewCheck() {
        return this.findReviewCheckData().length > 0;
    }
    async doReviewCheck() {
        if (!this.isReviewCheck()) {
            swal({
                title: '警告',
                text: '没有数据可以进行批量撤销',
                type: 'warning',
                width: 300
            });
            return;
        }
        var arr = this.findReviewCheckData();
        let promise = new Promise((resolve, reject) => {
            layer.confirm('请确认是否复检选中数据？<br/>' + arr.map(e => e.code).join("<br\>"), {
                btn: ['确定', '取消'], title: "警告！！！",
                btn1: function () {
                    resolve(true);
                    return false;
                },
                btn2: function () {
                    resolve(false);
                    return false;
                }
            });
            $('.layui-layer').width('460px');
        });
        let result = await promise;
        if (result) {
            var _url = ctx + "/commissionBill/doBatchReviewCheck.action";
            var idlist = arr.map(e => e.id);
            $.ajax({
                type: "POST",
                url: _url,
                data: JSON.stringify(idlist),
                processData: true,
                dataType: "json",
                contentType: 'application/json;charset=utf-8',
                async: true,
                success: function (data) {
                    if (data.code == "200") {
                        TLOG.component.operateLog('登记单管理', "批量复检", '【IDS】:' + JSON.stringify(idlist));
                        layer.alert('操作成功', {
                            title: '操作',
                            time: 600,
                            end: function () {
                                layer.closeAll();
                                queryRegisterBillGrid();
                            }
                        }, function () {
                            layer.closeAll();
                            queryRegisterBillGrid();
                        });
                    }
                    else {
                        layer.closeAll();
                        swal('错误', data.result, 'error');
                    }
                },
                error: function () {
                    layer.closeAll();
                    swal('错误', '远程访问失败', 'error');
                }
            });
        }
        layer.closeAll();
    }
    filterByProp(prop, propValues) {
        let arrayData = $.makeArray(this.rows);
        let arrayValue = $.makeArray(propValues);
        let values = _.chain(arrayData).filter(element => $.inArray(element[prop], arrayValue) > -1).value();
        return values;
    }
    initAutoComplete(selector, url) {
        $(selector).keydown(function (e) {
            if (e.keyCode == 13) {
            }
        });
        $(selector).data('oldvalue', '');
        $(selector).on('change', function () {
            var oldvalue = $(selector).data('oldvalue');
            var val = $(this).val();
            if (oldvalue != val) {
                $(this).siblings('input').val('');
            }
        });
        $(selector).devbridgeAutocomplete({
            noCache: 1,
            serviceUrl: url,
            dataType: 'json',
            onSearchComplete: function (query, suggestions) {
            },
            showNoSuggestionNotice: true,
            noSuggestionNotice: "不存在，请重输！",
            autoSelectFirst: true,
            autoFocus: true,
            onSelect: function (suggestion) {
                console.info('onSelect');
                var self = this;
                var idField = $(self).siblings('input');
                idField.val(suggestion.id);
                $(self).val(suggestion.value.trim());
                $(selector).data('oldvalue', suggestion.value);
                var v = $(self).valid();
            }
        });
    }
}
//# sourceMappingURL=commissionbill.create.js.map