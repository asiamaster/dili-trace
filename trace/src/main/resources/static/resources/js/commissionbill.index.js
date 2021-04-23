class CommissionBillGrid extends ListPage {
    constructor(grid, queryform, toolbar) {
        super(grid, queryform, $('#query'), "/commissionBill/listPage.action");
        this.toolbar = toolbar;
        this.btns = this.toolbar.find('button');
        window['commissionBillGrid'] = this;
        $('#add-btn').on('click', async () => await this.openCreatePage());
        $('#detail-btn').on('click', async () => await this.doDetail());
        $('#audit-btn').on('click', async () => await this.audit());
        $('#createsheet-btn').on('click', async () => await this.doCreateCheckSheet());
        $('#batch-reviewCheck-btn').on('click', async () => await this.doReviewCheck());
        let categoryController = new CategoryController();
        let cityController = new CityController();
        this.initAutoComplete($("[name='productName']"), function (query, done) {
            categoryController.lookupCategories(query, done);
        });
        this.initAutoComplete($("[name='originName']"), function (query, done) {
            cityController.lookupCities(query, done);
        });
        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.checkAndShowHideBtns());
    }
    doCreateCheckSheet() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        var idList = row.map(function (v, i) {
            return v.id;
        });
        let param = $.param({ idList: idList }, true);
        let url = this.toUrl("/checkSheet/edit.html?" + param);
        var audit_dia = bs4pop.dialog({
            title: '创建打印报告单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '50%',
            height: '70%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    audit() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        if (row.length > 1) {
            bs4pop.alert("请选择数据过多", { type: 'warning' });
            return;
        }
        console.log(row);
        let url = this.toUrl("/commissionBill/audit.html?billId=" + row[0].id);
        var audit_dia = bs4pop.dialog({
            title: '审核',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '50%',
            height: '51%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    doAudit(id) {
        let url = this.toUrl("/commissionBill/doAuditCommissionBillByManager.action");
        $.ajax({
            type: "POST",
            data: { billId: id },
            url: url,
            processData: true,
            dataType: "json",
            async: true,
            success: function (ret) {
                if (ret.success) {
                    bs4pop.alert("操作成功", { type: 'success' }, function () {
                        window['commissionBillGrid'].removeAllAndLoadData();
                    });
                }
            }
        });
    }
    resetButtons() {
        var btnArray = ['detail-btn', 'createsheet-btn', 'audit-btn', 'batch-reviewCheck-btn'];
        $.each(btnArray, function (i, btnId) {
            $('#' + btnId).hide();
        });
    }
    async checkAndShowHideBtns() {
        this.resetButtons();
        var rows = this.rows;
        if (rows.length == 0) {
            return;
        }
        if (this.isReviewCheck()) {
            $('#batch-reviewCheck-btn').show();
        }
        var exists_detectState_pass = _.chain(rows).filter(item => {
            return 50 == item.detectStatus;
        }).filter(item => {
            return !_.isUndefined(item.checkSheetId);
        }).filter(item => {
            return !_.isNull(item.checkSheetId);
        }).filter(item => {
            return !_.isEmpty(item.checkSheetId);
        }).value().length > 0;
        var hasName = false;
        var hasCorporateName = false;
        var rowsArray = $.makeArray(rows);
        var nameArray = _.chain(rows).map(item => item.name).filter(item => !_.isEmpty(item)).value();
        if (exists_detectState_pass) {
            var distinctNameArray = nameArray.reduce(function (accumulator, currentValue, index, array) {
                if ($.inArray(currentValue, array, index + 1) == -1) {
                    accumulator.push(currentValue);
                }
                return accumulator;
            }, []);
            var corporateNameArray = _.chain(rows).map(item => item.corporateName).filter(item => !_.isEmpty(item)).value();
            var distinctCorporateNameArray = corporateNameArray.reduce(function (accumulator, currentValue, index, array) {
                if ($.inArray(currentValue, array, index + 1) == -1) {
                    accumulator.push(currentValue);
                }
                return accumulator;
            }, []);
            $('#createsheet-btn').hide();
            if (rowsArray.length == corporateNameArray.length && distinctCorporateNameArray.length == 1) {
                $('#createsheet-btn').show();
            }
            else if (rowsArray.length == nameArray.length && distinctCorporateNameArray.length == 0 && distinctNameArray.length == 1) {
                $('#createsheet-btn').show();
            }
            else {
                $('#createsheet-btn').hide();
            }
        }
        else {
            $('#createsheet-btn').hide();
        }
        if (rows.length > 1) {
            return;
        }
        var selected = rows[0];
        if (0 == selected.verifyStatus) {
            $('#audit-btn').show();
        }
        else {
            $('#audit-btn').hide();
        }
        $('#detail-btn').show();
    }
    removeAllAndLoadData() {
        bs4pop.removeAll();
        (async () => {
            await super.queryGridData();
        })();
    }
    openCreatePage() {
        let url = this.toUrl("/commissionBill/create.html");
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
    doDetail() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        if (row.length > 1) {
            bs4pop.alert("请选择数据过多", { type: 'warning' });
            return;
        }
        let selected_id = row[0].id;
        let url = this.toUrl('/commissionBill/view.html?id=' + selected_id );
        var dia = bs4pop.dialog({
            title: '查看委托单',
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
        return this.grid.bootstrapTable("getSelections");
    }
    findReviewCheckData() {
        return _.chain(this.rows).filter(item => {
            return 50 == item.detectStatus;
        }).filter(item => !_.isUndefined(item.detectRequest)).filter(item => {
            return 2 == item.detectRequest.detectResult;
        }).value();
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
}
//# sourceMappingURL=commissionbill.index.js.map