<script type="text/javascript">
    let filedNameRetMap = JSON.parse('${filedNameRetMap}');
    let prefix = "${imageViewPathPrefix}/";

    let measureTypeEnumList = JSON.parse('${measureTypeEnumList}');
    let imageCertTypeEnumList=JSON.parse('${imageCertTypeEnumList}');
    let truckTypeEnumEnumList=JSON.parse('${truckTypeEnumEnumList}').sort(function(a,b){
        return b-a;
    });
    let measureTypeOptions = [];
    if (filedNameRetMap.measureType && filedNameRetMap.measureType.displayed === 1 && filedNameRetMap.measureType.availableValueList) {
        measureTypeEnumList.forEach(mt => {
            if ($.inArray(new String(mt.code).toString(), filedNameRetMap.measureType.availableValueList) > -1) {
                measureTypeOptions.push({text: mt.name, value: mt.code})
            }
        })
    }
    let defaultMeasureType=measureTypeOptions.length > 0 ? measureTypeOptions[measureTypeOptions.length - 1]:null

    let truckTypeOptions = [];
    if (filedNameRetMap.truckType && filedNameRetMap.truckType.displayed === 1 && filedNameRetMap.truckType.availableValueList) {
        truckTypeEnumEnumList.forEach(mt => {
            if ($.inArray(new String(mt.code).toString(), filedNameRetMap.truckType.availableValueList) > -1) {
                if(mt.code===10){
                    truckTypeOptions.push({text: '否', value: mt.code})
                }else{
                    truckTypeOptions.push({text: '是', value: mt.code})
                }

            }
        })
    }
    let defaultruckTypeValue=truckTypeOptions.length > 0 ? truckTypeOptions[0].value : '';
    let defaultFormData={
        registerHeadWeight :'',
        registerHeadRemainWeight :'',
        imageCertList:[],
        pieceNum: "",
        pieceweight: "",
        measureType: defaultMeasureType!=null ? defaultMeasureType.value : '',
        registType: 10,
        userId: "",
        registerHeadCode: "",
        arrivalTallynos: "",
        plateList: [],
        truckType: defaultruckTypeValue,
        weight: "",
        weightUnit: 1,
        productName: "",
        originName: "",
        unitPrice: '',
        originId:'',
        truckTareWeight:'0',
        remark:'',
    }
    var app = new Vue({
        el: '#app',
        created: function () {
            let vm = this;
            $.each(imageCertTypeEnumList,function(){
                let certTypeCode=this.code;
                let uniqueCertTypeName="certType"+certTypeCode;
                let label=this.name;
                vm.formConfig.formDesc[uniqueCertTypeName] = {
                    type: function(formData) {
                        if(formData.registType=='30'){
                            return 'image';
                        }else{
                            return 'image-uploader'
                        }
                    },//"image-uploader",
                    label: label,
                    attrs: {
                        name: "file",
                        multiple: true,
                        action: "/imageController/upload.action",
                        responseFn(response, file) {
                            let imageCertList=app.formData.imageCertList
                            imageCertList.push({certType: certTypeCode, uid: response.data});

                            app.formData.imageCertList=imageCertList
                            return prefix + response.data;
                        },
                        beforeRemove: function (file, c) {
                            let imageCertList=app.formData.imageCertList
                            app.formData.imageCertList=  imageCertList.splice(imageCertList.findIndex(item => item.uid === file.replace(prefix, "")), 1);

                        },

                        fileType: ["jpg", "png", "bmp"],
                        limit: "10",
                    },
                    vif: function () {
                        return filedNameRetMap.imageCertList.displayed === 1;
                    }
                }
            });

            init(this);
            let formDataRef = this.formData;
            if(formDataRef.pieceWeight){
                formDataRef.pieceweight=formDataRef.pieceWeight;
            }
        },
        watch:{
            'formData.imageCertList': {
                deep: true,
                handler: function (imageCertList, oldValue) {
                    let certTypeMap={};
                    $.each(imageCertTypeEnumList,function(){
                        let certTypeName="certType"+this.code;
                        certTypeMap[this.code.toString()]=certTypeName;
                        console.info(certTypeName)
                        app.formData[certTypeName]=[];
                    });

                    let groupedImageList={};
                    $.each(imageCertList,function(k,v){
                        let certType=this.certType.toString();
                        let key=certTypeMap[certType];
                        if(!groupedImageList[key]){
                            groupedImageList[key]=[];
                        }
                        groupedImageList[key].push(this);
                    });
                    $.each(groupedImageList,function(k,v){
                        app.formData[k]=v.map(img=>prefix+img.uid);
                    });
                }
            },
            'formData.truckType': {
                deep: true,
                handler: function (truckType, oldValue) {
                    app.rules.plate.required = (truckType===20);
                    app.rules.plateList.required = (truckType===20);
                }
            },
            'formData.measureType': {
                deep: true,
                handler: function (measureType, oldValue) {
                    app.formConfig.formDesc.weight.rules[1].required=(measureType === 20);
                    if(measureType!='20'){
                        let pieceNum=app.formData.pieceNum;
                        let pieceWeight=app.formData.pieceWeight;

                        if(pieceNum!=''&&pieceWeight!=''){
                            if(!isNaN(pieceNum)&&!isNaN(pieceWeight)&&app.formData.weight!=pieceNum * pieceWeight){
                                app.formData.weight = pieceNum * pieceWeight;
                            }
                        }
                    }
                }
            },
            'formData.registerHeadCode': {
                deep: true,
                handler: function (registerHeadCode, oldValue) {
                    if(typeof(oldValue)=='undefined'){
                        oldValue=''
                    }
                    if(typeof(registerHeadCode)=='undefined'){
                        registerHeadCode='';
                    }

                    if(oldValue===registerHeadCode){
                        return;
                    }

                    var newFormData = $.extend(true,{}, defaultFormData);

                    let obj=null;
                    if(app?.registerHeadList&&app.registerHeadList.length>0){
                        let registerHeadListRef = app.registerHeadList;
                        let selectedList=registerHeadListRef.filter(item=>item.code===registerHeadCode);
                        obj=selectedList.length>0?selectedList[selectedList.length-1]:null;
                    }
                    if(obj==null){
                        $.extend(app.formData,newFormData);
                        return;
                    }
                    app.formConfig.formDesc.truckType.options=truckTypeOptions.filter(item=>item.value==obj.truckType);
                    if(app.formConfig.formDesc.truckType.options.length==0){
                        bs4pop.alert('是否拼车配置错误,请联系管理员', {type: 'error'});
                        return;
                    }
                    app.formConfig.formDesc.measureType.options=measureTypeOptions.filter(item=>item.value==obj.measureType)
                    if(app.formConfig.formDesc.measureType.options.length==0){
                        bs4pop.alert('计重方式配置错误,请联系管理员', {type: 'error'});
                        return;
                    }
                    let registerHeadId=obj.id;
                    let data={};
                    data.registerHeadWeight = obj.weight;
                    data.registerHeadRemainWeight = obj.remainWeight;
                    data.measureType = obj.measureType;
                    data.truckType = obj.truckType;
                    data.specName = obj.specName;
                    data.brandName = obj.brandName;
                    data.truckTareWeight = obj.truckTareWeight;
                    data.arrivalDatetime = obj.arrivalDatetime;
                    data.originId = obj.originId;
                    data.originName = obj.originName;
                    data.productId = obj.productId;
                    data.productName = obj.productName;
                    data.upStreamId = obj.upStreamId;
                    data.unitPrice = obj.unitPrice;
                    data.weightUnit=obj.weightUnit;

                    data.arrivalTallynos=obj.arrivalTallynos;
                    data.remark=obj.remark;

                    data.imageCertList=obj.imageCertList;
                    $.each(data,function(k,v){
                        if(typeof (v)=='undefined'){
                            data[k]=defaultFormData[k];
                        }
                    })
                    let formData=$.extend(true,{},app.formData,data);
                    app.formData=formData;

                }
            }
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
                loading:false,
                editMode: false,
                imageCertList: [],
                registerHeadList: JSON.parse('${registerHeadList}'),
                weightUnit: "",
                productOptionsTemp: [],
                rules: {
                    plate: {required: defaultruckTypeValue=='20', type: 'string', message: '必须填写车牌'},
                    plateList: {required: true, type: 'string', message: '必须填写车牌'},
                    registerHeadCode: {required: false, type: 'string', message: '必须选择台账'},

                },
                formData: $.extend(true,{},defaultFormData),
                formConfig: {
                    formBtnSize: 'small',
                    formAttrs: {size: 'small'},
                    isShowBackBtn: false,
                    formBtns: [
                        {
                            text: '关闭',
                            click: () => {
                                if (parent && parent.bs4pop) {
                                    parent.bs4pop.removeAll();
                                }
                            }
                        }
                    ],
                    formDesc: {
                        registType: {
                            options: loadProvider({provider: 'registTypeProvider', queryParams: {required: true}}),
                            type: "select",
                            label: "单据类型",
                            required: true
                        },
                        userId: {
                            type: "select",
                            label: "经营户",
                            required: true,
                            prop: {text: 'text', value: 'id'},
                            options: async data => {
                                if (this.editMode) {
                                    const list = await axios.post('/customer/listSeller.action', {'id': data.userId});
                                    return $.map(list.data.data, function (obj) {
                                        obj.text = $.makeArray([obj.name, obj.phone, obj.cardNo, obj.marketName]).filter(v=>v).join("|")
                                        return obj;
                                    });
                                }
                            },
                            attrs: {
                                placeholder: "编号,姓名,手机号,卡号",
                                filterable: true,
                                remote: true,
                                remoteMethod(query, callback) {
                                    axios.post('/customer/listSeller.action', {'keyword': query})
                                        .then((res) => {
                                            const data = $.map(res.data.data, function (obj) {
                                                obj.text = $.makeArray([obj.name, obj.phone, obj.cardNo, obj.marketName]).filter(v=>v).join("|")
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
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.arrivalTallynos.required === 1,
                            prop: {text: 'text', value: 'text'},
                            options: async data => {
                                let registerHeadCode = data.registerHeadCode;
                                if (registerHeadCode == undefined || registerHeadCode == '' || registerHeadCode == null) {
                                    if(!data||!data.userId||data.userId==''){
                                        return [];
                                    }
                                    const list = await axios.post(
                                        '/leaseOrderRpc/findLease.action',
                                        {customerId: data.userId}
                                    )
                                    return $.map(list.data.data, function (obj) {
                                        return {text: obj};
                                    });
                                } else {
                                    let registerHeadList = this.registerHeadList;
                                    for (let i = 0; i < registerHeadList.length; i++) {
                                        let obj = registerHeadList[i];
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
                            optionsLinkageFields: ['userId','registType'],
                            prop: {text: 'text', value: 'code'},
                            options: async data => {
                                if(!data||!data.userId||data.userId==''){
                                    return
                                }
                                const list = await axios.post(
                                    '/registerHead/listRegisterHead.action',
                                    {userId: data.userId, minRemainWeight: 0}
                                )
                                this.registerHeadList = list.data.data;
                                return $.map(list.data.data, function (obj) {
                                    obj.text = obj.productName + ' | ' + obj.weight + obj.weightUnitName + ' | ' + obj.created;
                                    return obj;
                                });
                            },
                            vif: function (form) {
                                return form.registType === 30;

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
                            options: truckTypeOptions,
                            required: filedNameRetMap.truckType.required === 1,
                            type: "radio",
                            label: "是否拼车",
                            vif: function () {
                                return filedNameRetMap.truckType.displayed === 1;
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
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                let registerHeadList = this.registerHeadList;
                                for (let i = 0; i < registerHeadList.length; i++) {
                                    let obj = registerHeadList[i];
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
                            required: true,
                            prop: {text: 'name', value: 'id'},
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this !== undefined) {
                                    let registerHeadList = this.registerHeadList;
                                    for (let i = 0; i < registerHeadList.length; i++) {
                                        let obj = registerHeadList[i];
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
                            options: measureTypeOptions,
                            type: "radio",
                            label: "计重方式",
                            required: filedNameRetMap.measureType.required === 1,
                            vif: function () {
                                return filedNameRetMap.measureType.displayed === 1;
                            } /*,on:{
                                change:function (val){
                                    //el-input-group
                                    let weightUnit=$('label[for="weight"]').parent().parent().find('.el-input-group__append')
                                    let pieceWeightUnit=$('label[for="pieceweight"]').parent().parent().find('.el-input-group__append')

                                    app.formConfig.formDesc.weight.rules[1].required=(val === 20);
                                    if(val===20){
                                        pieceWeightUnit.data('value-type','skip');
                                        pieceWeightUnit.hide();

                                        weightUnit.show();
                                        app.formConfig.formDesc.weight.rules[1].required=true;
                                    }else{

                                        weightUnit.data('value-type','skip');
                                        weightUnit.hide();

                                        pieceWeightUnit.show();
                                        app.formConfig.formDesc.weight.rules[1].required=false;
                                    }

                                }
                            },*/
                        },
                        weight: {
                            type: function(formData) {
                                if(formData.measureType=='20'){
                                    return 'input';
                                }else{
                                    return 'text'
                                }
                            },
                            disabled:true,
                            rules: [{
                                pattern: /^(\s{0,0}|([1-9][0-9]{0,7}))$/,
                                message: "请输入1-99999999之间的数字"
                            },{ required:(defaultMeasureType!=null?defaultMeasureType.value==20 : false),message:"商品重量不能为空"}],
                            label: function(){
                                let checkedMeasureType='10';
                                if(app?.formData){
                                    checkedMeasureType= app.formData.measureType;
                                }else{
                                    checkedMeasureType=defaultMeasureType!=null?defaultMeasureType.value : '10'
                                }
                                if(checkedMeasureType=='20'){
                                    return '商品重量';
                                }else{
                                    return '合计'
                                }
                            }

                        },
                        pieceNum: {
                            type: "input",
                            label: "商品件数",
                            required: $.inArray('10',filedNameRetMap.measureType.availableValueList)>-1,
                            rules: [{
                                pattern: /^([1-9][0-9]{0,6})$/,
                                message: "请输入1-9999999之间的数字"
                            }],
                            vif: function (form) {
                                return form.measureType !== 20;
                            },
                            on: {
                                input: function (value) {

                                    let formDataRef = app.formData;
                                    if(formDataRef?.pieceweight!=''){
                                        if(!isNaN(formDataRef.pieceweight)&&!isNaN(value)){
                                            formDataRef.weight = formDataRef.pieceweight * value;
                                            return;
                                        }
                                    }
                                    formDataRef.weight = '';

                                }
                            }
                        },
                        "pieceweight": {
                            type: "input",
                            label: "件重",
                            required: $.inArray('10',filedNameRetMap.measureType.availableValueList)>-1,
                            rules: [{
                                pattern: /^([1-9][0-9]{0,6})$/,
                                message: "请输入1-9999999之间的数字"
                            }],
                            vif: function (form) {
                                return form.measureType !== 20;

                            }
                        },

                        specName: {
                            type: "input",
                            label: "商品规格",
                            rules: [{
                                pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9_]{0,10}$/,
                                message: "请输入不超过10个长度中英文以及下划线"
                            }],
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.specName.required === 1,
                            vif: function () {
                                return filedNameRetMap.specName.displayed === 1;
                            }
                        },
                        brandName: {
                            type: "autocomplete",
                            label: "品牌",
                            rules: [{
                                pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9_]{0,20}$/,
                                message: "请输入不超过20个长度中英文以及下划线"
                            }],
                            disabled: function (data) {
                                return data.registType === 30
                            },
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
                            prop: {text: 'mergerName', value: 'id'},
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.originId.required === 1,
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this != undefined) {
                                    let registerHeadList = this.registerHeadList;
                                    for (let i = 0; i < registerHeadList.length; i++) {
                                        let obj = registerHeadList[i];
                                        if (obj.code === data.registerHeadCode) {
                                            return [{mergerName: obj.originName, id: obj.originId}]
                                        }
                                    }
                                }
                                if (this.editMode) {
                                    return [{mergerName: data.originName, id: data.originId}]
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
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.upStreamId.required === 1,
                            optionsLinkageFields: ['registerHeadCode'],
                            options: data => {
                                if (this !== undefined) {
                                    let registerHeadList = this.registerHeadList;
                                    for (let i = 0; i < registerHeadList.length; i++) {
                                        let obj = registerHeadList[i];
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
                            type: "number",
                            label: "皮重",
                            rules: [{
                                pattern: /^([1-9][0-9]{0,6})$/,
                                message: "请输入1-9999999之间的数字"
                            }],
                            required: filedNameRetMap.truckTareWeight.required === 1,
                            vif: function () {
                                return filedNameRetMap.truckTareWeight.displayed === 1;
                            }
                        },
                        arrivalDatetime: {
                            type: "datetime",
                            label: "到场时间",
                            attrs: {
                                valueFormat:'yyyy-MM-dd HH:mm:ss'
                            },
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.arrivalDatetime.required === 1,
                            vif: function () {
                                return filedNameRetMap.arrivalDatetime.displayed === 1;
                            }
                        },
                        unitPrice: {
                            type: "input",
                            label: "商品单价",
                            rules: [{
                                pattern: /^(\s{0,0}|\d\.([1-9]{1,2}|[0-9][1-9])|[1-9]\d{0,3}(\.\d{1,2}){0,1})$/,
                                message: "请输入0.01-9999.99之间的数字(两位小数)"
                            }],
                            attrs: {},
                            disabled: function (data) {
                                return data.registType === 30
                            },
                            required: filedNameRetMap.unitPrice.required === 1,
                            vif: function () {
                                return filedNameRetMap.unitPrice.displayed === 1;
                            }
                        },
                        remark: {
                            type: "textarea",
                            label: "备注",
                            rules: [{
                                pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9_]{0,200}$/,
                                message: "请输入不超过200个长度中英文以及下划线"
                            }],
                            disabled: function (data) {
                                return data.registType === 30
                            },
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

                        "pieceNum",
                        'pieceweight',
                        "weight",
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
                app.loading=true;
                //registerBill.imageCertList = this.imageCertList;

                let data = registerBill;
                let url = '/newRegisterBill/doAdd.action'
                if (app.editMode) {
                    url = '/newRegisterBill/doEdit.action'
                }
                axios.post(url, data)
                    .then((res) => {
                        app.loading=false;
                        if (!res.data.success) {
                            bs4pop.alert(res.data.message, {type: 'error'});
                            return;
                        }
                        bs4pop.removeAll();
                        bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
                        if (parent && parent.window['RegisterBillGridObj']) {
                            parent.window['RegisterBillGridObj'].removeAllAndLoadData()
                        }
                    })
                    .catch(function (error) {
                        app.loading=false;
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    });
            },
            changePieceWeight: function (value) {
                this.formData['pieceweight']=value;
                this.formData['pieceWeight']=value;
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
