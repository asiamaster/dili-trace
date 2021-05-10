<script type="text/javascript">
    let filedNameRetMap = JSON.parse('${filedNameRetMap}');
    var app = new Vue({
        el: '#app',
        created: function () {
            let imageCertTypeEnumMap = JSON.parse('${imageCertTypeEnumMap}');
            let prefix = "${imageViewPathPrefix}/";
            let vm = this;
            imageCertTypeEnumMap.forEach(it => {
                vm.formConfig.formDesc["certType" + it.certType] = {
                    type: "image-uploader",
                    label: it.certTypeName,
                    attrs: {
                        name: "file",
                        multiple: true,
                        action: "/imageController/upload.action",
                        responseFn(response, file) {
                            vm.imageCertList.push({certType: it.certType, uid: response.data})
                            return prefix + response.data;
                        },
                        beforeRemove: function (file, fileList) {
                            vm.imageCertList.splice(vm.imageCertList.findIndex(item => item.uid === file.replace(prefix, "")), 1);
                        },
                        limit: "10"
                    },
                    vif: function () {
                        return filedNameRetMap.imageCertList.displayed === 1;
                    }
                }
            })
            init(this);
        },
        data() {
            return {
                unitOptions: [{
                    value: 1,
                    label: '斤'
                }, {
                    value: 2,
                    label: '公斤'
                }],
                editMode: false,
                imageCertList: [],
                registerHeadCodeTemp: [],
                weightUnit: "",
                pieceweightUnit: "",
                productOptionsTemp: [],
                rules: {
                    plate: {required: true, type: 'string', message: '必须填写车牌'},
                    plateList: {required: true, type: 'string', message: '必须填写车牌'},
                },
                formData: {
                    total: 0,
                    pieceNum: 0,
                    pieceweight: "",
                    measureType: 20,
                    registType: 10,
                    userId: "",
                    registerHeadCode: "",
                    arrivalTallynos: "",
                    plateList: [],
                    truckType: 20,
                    weight: 0,
                    weightUnit: 1,
                    productName: "",
                    originName: "",
                    pieceweightUnit: 1,
                },
                formConfig: {
                    formBtnSize: 'small',
                    formAttrs: {size: 'small'},
                    isShowBackBtn:false,
                    formBtns: [
                        {
                            text: '关闭',
                            click: () => {
                                if(parent&&parent.bs4pop){
                                    parent.bs4pop.removeAll();
                                }
                            }
                        }
                    ],
                    formDesc: {
                        registType: {
                            options: loadProvider({provider: 'registTypeProvider', queryParams: {required: true}}),
                            type: "select",
                            label: "单据类型"
                        },
                        userId: {
                            type: "select",
                            label: "卖家姓名",
                            prop: {text: 'text', value: 'id'},
                            options: async data => {
                                if (this.editMode) {
                                    const list = await axios.post('/customer/listSeller.action', {'id': data.userId});
                                    return $.map(list.data.data, function (obj) {
                                        let cardNo = obj.tradePrintingCard;
                                        obj.text = obj.name + ' | ' + obj.phone + ' | ' + cardNo + ' | ' + obj.marketName
                                        return obj;
                                    });
                                }
                            },
                            attrs: {
                                filterable: true,
                                remote: true,
                                remoteMethod(query, callback) {
                                    axios.post('/customer/listSeller.action', {'keyword': query})
                                        .then((res) => {
                                            const data = $.map(res.data.data, function (obj) {
                                                let cardNo = obj.tradePrintingCard;
                                                obj.text = obj.name + ' | ' + obj.phone + ' | ' + cardNo + ' | ' + obj.marketName
                                                return obj;
                                            });
                                            callback(data);
                                        })
                                        .catch(function (error) {
                                            callback([]);
                                        });
                                }
                            }
                        },
                        arrivalTallynos: {
                            type: "select",
                            label: "到货摊位",
                            optionsLinkageFields: ['userId', 'registerHeadCode'],
                            required: filedNameRetMap.arrivalTallynos.required === 1,
                            prop: {text: 'text', value: 'text'},
                            options: async data => {
                                let registerHeadCode = data.registerHeadCode;
                                if (registerHeadCode == undefined || registerHeadCode == '' || registerHeadCode == null) {
                                    const list = await axios.post(
                                        '/leaseOrderRpc/findLease.action',
                                        {customerId: data.userId}
                                    )
                                    return $.map(list.data.data, function (obj) {
                                        return {text: obj};
                                    });
                                } else {
                                    let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                    for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                        let obj = registerHeadCodeTemp[i];
                                        if (obj.code === registerHeadCode) {
                                            return $.map(obj.arrivalTallynos, function (obj) {
                                                return {text: obj}
                                            });
                                        }
                                    }
                                }

                            },
                            attrs: {
                                filterable: true,
                                multiple: true,
                                allowCreate: true
                            },
                            vif: function () {
                                return filedNameRetMap.arrivalTallynos.displayed === 1;
                            }
                        },
                        registerHeadCode: {
                            type: "select",
                            label: "台账",
                            optionsLinkageFields: ['userId'],
                            prop: {text: 'text', value: 'code'},
                            options: async data => {
                                const list = await axios.post(
                                    '/registerHead/listRegisterHead.action',
                                    {userId: data.userId, minRemainWeight: 0}
                                )
                                this.registerHeadCodeTemp = list.data.data;
                                return $.map(list.data.data, function (obj) {
                                    obj.text = obj.productName + ' | ' + obj.weight + obj.weightUnitName + ' | ' + obj.created;
                                    return obj;
                                });
                            },
                            vif: function (form) {
                                return form.registType === 30;

                            },
                            on: {
                                change: function (val) {
                                    let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                    for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                        let obj = registerHeadCodeTemp[i];
                                        if (obj.code === val) {
                                            this.formData.registerHeadWeight = obj.weight;
                                            this.formData.registerHeadRemainWeight = obj.remainWeight;
                                            this.formData.measureType = obj.measureType;
                                            this.formData.truckType = obj.truckType;
                                            this.formData.specName = obj.specName;
                                            this.formData.brandName = obj.brandName;
                                            this.formData.truckTareWeight = obj.truckTareWeight;
                                            this.formData.arrivalDatetime = obj.arrivalDatetime;
                                            this.formData.originId = obj.originId;
                                            this.formData.originName = obj.originName;
                                            this.formData.productId = obj.productId;
                                            this.formData.productName = obj.productName;
                                            this.formData.upStreamId = obj.upStreamId;
                                        }
                                    }
                                }
                            }
                        },
                        registerHeadWeight: {
                            isOptions: true,
                            default: "0",
                            type: "text",
                            label: "主台账重量",
                            vif: function (form) {
                                return form.registType === 30;

                            }
                        },
                        registerHeadRemainWeight: {
                            default: "0",
                            type: "text",
                            label: "待进门重量",
                            vif: function (form) {
                                return form.registType === 30;

                            }
                        },
                        truckType: {
                            options: [
                                {
                                    text: "是",
                                    value: 20
                                },
                                {
                                    text: "否",
                                    value: 10
                                }
                            ],
                            required: filedNameRetMap.truckType.required === 1,
                            type: "radio",
                            label: "是否拼车",
                            vif: function () {
                                return filedNameRetMap.truckType.displayed === 1;
                            },
                            on: {
                                change: function (val) {
                                    app.rules['plate'].required = val === 20;
                                    app.rules['plateList'].required = val === 20;
                                }
                            }
                        },
                        plate: {
                            type: "input",
                            label: "车牌号",
                            vif: function (form) {
                                if (filedNameRetMap.truckType.displayed === 0) {
                                    return false;
                                }
                                return form.registType !== 30;
                            },
                        },
                        plateList: {
                            type: "select",
                            label: "车牌号",
                            prop: {text: 'text', value: 'text'},
                            required: true,
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                    let obj = registerHeadCodeTemp[i];
                                    if (obj.code === data.registerHeadCode) {
                                        return $.map(obj.plateList, function (obj) {
                                            return {text: obj};
                                        });
                                    }
                                }
                                return [];
                            },
                            vif: function (form) {
                                if (filedNameRetMap.truckType.displayed === 0) {
                                    return false;
                                }
                                return form.registType === 30;
                            },
                        },
                        productId: {
                            type: "select",
                            label: "商品品类",
                            prop: {text: 'name', value: 'id'},
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this !== undefined) {
                                    let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                    for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                        let obj = registerHeadCodeTemp[i];
                                        if (obj.code === data.registerHeadCode) {
                                            return [{name: obj.productName, id: obj.productId}]
                                        }
                                    }
                                }
                                if (this.editMode) {
                                    return [{name: data.productName, id: data.productId}]
                                }
                                return [];
                            },
                            attrs: {
                                filterable: true,
                                remote: true,
                                remoteMethod(query, callback) {
                                    axios.post('/category/listCategories.action', {'keyword': query})
                                        .then((res) => {
                                            const data = $.map(res.data.data, function (obj) {
                                                return obj;
                                            });
                                            callback(data);
                                        })
                                        .catch(function (error) {
                                            callback([]);
                                        });
                                }
                            },
                            on: {
                                change: function (val) {
                                    let options = app.$refs.myForm.$refs.productId[0].options;
                                    options.forEach(it => {
                                        if (it.value === val) {
                                            app.formData.productName = it.text;
                                        }
                                    });
                                }
                            }
                        },
                        measureType: {
                            options: [
                                {
                                    text: "计重",
                                    value: 20
                                },
                                {
                                    text: "计件",
                                    value: 10
                                }
                            ],
                            type: "radio",
                            label: "计重方式",
                            required: filedNameRetMap.measureType.required === 1,
                            vif: function () {
                                return filedNameRetMap.measureType.displayed === 1;
                            }
                        },
                        weight: {
                            type: "input",
                            label: "商品重量",
                            vif: function (form) {
                                return form.measureType === 20;
                            },
                        },
                        pieceNum: {
                            type: "input",
                            label: "商品件数",
                            rules: [{
                                pattern: new RegExp("^\\d+$", ""),
                                type: "string",
                                message: "格式不正确"
                            }],
                            vif: function (form) {
                                return form.measureType !== 20;

                            },
                            on: {
                                input: function (value) {
                                    if (!value || !this.formData.pieceweight) {
                                        this.formData.total = 0;
                                    } else {
                                        this.formData.total = this.formData.pieceweight * value;
                                    }

                                }
                            }
                        },
                        pieceweight: {
                            type: "input",
                            label: "件重",
                            vif: function (form) {
                                return form.measureType !== 20;

                            }
                        },
                        total: {
                            isOptions: true,
                            default: "0",
                            type: "text",
                            label: "合计",
                            attrs: {},
                            vif: function (form) {
                                return form.measureType !== 20;

                            },
                        },
                        specName: {
                            type: "input",
                            label: "商品规格",
                            required: filedNameRetMap.specName.required === 1,
                            vif: function () {
                                return filedNameRetMap.specName.displayed === 1;
                            }
                        },
                        brandName: {
                            type: "autocomplete",
                            label: "品牌",
                            required: filedNameRetMap.brandName.required === 1,
                            attrs: {
                                valueKey: "brandName",
                                triggerOnFocus: false,
                                fetchSuggestions: function (queryString, cb) {
                                    axios.post("/brand/listBrands.action", {likeBrandName: queryString})
                                        .then(function (response) {
                                            cb(response.data.data);
                                        })
                                        .catch(function (error) {
                                        });
                                }
                            },
                            vif: function () {
                                return filedNameRetMap.brandName.displayed === 1;
                            }
                        },
                        originId: {
                            type: "select",
                            label: "产地",
                            prop: {text: 'name', value: 'id'},
                            required: filedNameRetMap.originId.required === 1,
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this != undefined) {
                                    let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                    for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                        let obj = registerHeadCodeTemp[i];
                                        if (obj.code === data.registerHeadCode) {
                                            return [{name: obj.originName, id: obj.originId}]
                                        }
                                    }
                                }
                                if (this.editMode) {
                                    return [{name: data.originName, id: data.originId}]
                                }
                                return [];
                            },
                            attrs: {
                                filterable: true,
                                remote: true,
                                remoteMethod(query, callback) {
                                    axios.post('/city/listCities.action', {'keyword': query})
                                        .then((res) => {
                                            const data = $.map(res.data.data, function (obj) {
                                                return obj;
                                            });
                                            callback(data);
                                        })
                                        .catch(function (error) {
                                            callback([]);
                                        });
                                }
                            },
                            on: {
                                change: function (val) {
                                    let options = app.$refs.myForm.$refs.originId[0].options;
                                    options.forEach(it => {
                                        if (it.value === val) {
                                            app.formData.originName = it.text;
                                        }
                                    });
                                }
                            },
                            vif: function () {
                                return filedNameRetMap.originId.displayed === 1;
                            }
                        },
                        upStreamId: {
                            type: "select",
                            label: "上游企业",
                            prop: {text: 'name', value: 'id'},
                            required: filedNameRetMap.upStreamId.required === 1,
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this !== undefined) {
                                    let registerHeadCodeTemp = this.registerHeadCodeTemp;
                                    for (let i = 0; i < registerHeadCodeTemp.length; i++) {
                                        let obj = registerHeadCodeTemp[i];
                                        if (obj.code === data.registerHeadCode) {
                                            return [{name: obj.upStreamName, id: obj.upStreamId}]
                                        }
                                    }
                                }
                                if (this.editMode) {
                                    return [{name: data.upStreamName, id: data.upStreamId}]
                                }
                                return [];
                            },
                            attrs: {
                                filterable: true,
                                remote: true,
                                // 相对于官方，增加了一个 callback 参数
                                remoteMethod(query, callback) {
                                    axios.post('/upStream/listByKeyWord.action', {
                                        'keyword': query,
                                        'userId': this.formData.userId
                                    })
                                        .then((res) => {
                                            const data = $.map(res.data.data, function (obj) {
                                                return obj;
                                            });
                                            callback(data);
                                        })
                                        .catch(function (error) {
                                            callback([]);
                                        });
                                }
                            },
                            vif: function () {
                                return filedNameRetMap.upStreamId.displayed === 1;
                            }
                        },
                        truckTareWeight: {
                            type: "input",
                            label: "皮重",
                            required: filedNameRetMap.truckTareWeight.required === 1,
                            vif: function () {
                                return filedNameRetMap.truckTareWeight.displayed === 1;
                            }
                        },
                        arrivalDatetime: {
                            type: "datetime",
                            label: "预计到场时间",
                            required: filedNameRetMap.arrivalDatetime.required === 1,
                            vif: function () {
                                return filedNameRetMap.arrivalDatetime.displayed === 1;
                            }
                        },
                        unitPrice: {
                            type: "number",
                            label: "商品单价",
                            attrs: {},
                            default: 0,
                            required: filedNameRetMap.unitPrice.required === 1,
                            vif: function () {
                                return filedNameRetMap.unitPrice.displayed === 1;
                            }
                        },
                        remark: {
                            type: "textarea",
                            label: "备注",
                            required: filedNameRetMap.remark.required === 1,
                            vif: function () {
                                return filedNameRetMap.remark.displayed === 1;
                            }
                        }
                    },
                    order: [
                        "registType",
                        "userId",
                        "registerHeadCode",
                        "registerHeadWeight",
                        "registerHeadRemainWeight",
                        "truckType",
                        "plate",
                        "plateList",
                        "productId",
                        "measureType",
                        "weight",
                        "pieceNum",
                        "pieceweight",
                        "total",
                        "specName",
                        "brandName",
                        "originId",
                        "upStreamId",
                        "truckTareWeight",
                        "arrivalDatetime",
                        "arrivalTallynos"
                    ]
                }
            };
        },
        methods: {
            handleRequest(registerBill) {
                if (registerBill.measureType == 10) {
                    registerBill.weight = registerBill.total;
                }
                registerBill.imageCertList = this.imageCertList;

                let data = registerBill;
                let url = '/newRegisterBill/doAdd.action'
                if (app.editMode) {
                    url = '/newRegisterBill/doEdit.action'
                }
                console.log(data);
                axios.post(url, data)
                    .then((res) => {
                        if (!res.data.success) {
                            bs4pop.alert(res.data.message, {type: 'error'});
                            return;
                        }
                        bs4pop.removeAll();
                        bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
                        if(parent&&parent.window['RegisterBillGridObj']){
                            parent.window['RegisterBillGridObj'].removeAllAndLoadData()
                        }
                    })
                    .catch(function (error) {
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    });
            },
            changePieceWeight: function (value) {
                if (!value || !this.formData.pieceNum) {
                    this.formData.total = 0;
                } else {
                    this.formData.total = this.formData.pieceNum * value;
                }
            }
        }
    });


    function loadProvider(data) {
        return new Promise((resolve, reject) => {
            axios.post('/convert/list.action', data)
                .then((res) => {
                    resolve(res.data);
                    // console.log(res);
                })
                .catch(function (error) {
                    reject(error);
                    // console.log(error);
                });
        });
    }
</script>
