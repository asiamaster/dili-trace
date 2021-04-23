class WebConfig{
    private contextPath:string
    private imageViewPathPrefix:string
    constructor() {
        let webGlobalConfig=window['webGlobalConfig'];
        if(_.isUndefined(webGlobalConfig)){
            console.error("没有初始化webGlobalConfig,请先引入webGlobalConfig.tag")
        }
        this.contextPath=webGlobalConfig.contextPath;
        this.imageViewPathPrefix=webGlobalConfig.imageViewPathPrefix;
        window.addEventListener('message', async (e) => this.handleMessage(e), false);
    }

    protected handleMessage(e: MessageEvent) {
        let cthis=this;
        let data = JSON.parse(e.data);
        if (data.type=='call'&&data.calls) {
            _.chain(data.calls).each(async (item)=>{
                let fun=item.funName;
                let args=item.args;
                await cthis[fun].call(cthis, args)
            });

        }if (data.type=='apply'&&data.calls) {
            _.chain(data.calls).each(async (item)=>{
                let fun=item.funName;
                let args=item.args;
                await cthis[fun].apply(cthis, args)
            });

        }
    }
    public toUrl(url:string):string{
        return this.contextPath+url;
    }

    private onselectFun(suggestion){
        // console.info('onSelect')
        var self = this;
        var forId=$(self).attr("for");
        if(!_.isEmpty(forId)){
            var idField = $('#'+forId);
            idField.val(suggestion.id);
        }
        $(self).val(suggestion.value.trim());
        $(self).data('input-text',$(self).val());
        //@ts-ignore
        var v=$(self).valid();
    }

    public initTraceAutoComplete(selector,lookupFun:Function,onSelect:Function=this.onselectFun){
        $(selector).keydown(function (e){
            if(e.keyCode == 13){
                // $(selector).data('keycode',e.keyCode);
                // console.info('keydown')
            }
        });
        $(selector).data('input-text','');
        $(selector).on('change',function () {
            var selectText=$(selector).data('select-text');
            var inputValue=$(selector).data('input-value');
            var val=$(this).val();
            if(selectText!=val){
                $(this).siblings('input').val('');
            }
            $(this).data('input-value', $(this).val());
        });
        $(selector).on('focus',function () {
            var selectText=$(selector).data('select-text');
            var inputValue=$(selector).data('input-value');
            if(_.isUndefined(inputValue)){
                $(this).val('')
            }else{
                $(this).val(inputValue)
            }
        });
        // 联想输入
        //@ts-ignore
        $(selector).devbridgeAutocomplete({
            noCache: false,
            //serviceUrl: url,  // 数据地址
            lookup: lookupFun,
            dataType: 'json',
            onSearchComplete: function (query, suggestions) {
            },
            deferRequestBy:200,
            triggerSelectOnValidInput:false,
            showNoSuggestionNotice: true,
            noSuggestionNotice: "不存在，请重输！",
            autoSelectFirst:true,
            autoFocus: false,
            onSelect: onSelect
        });
    }
    public serializeJSON(form:JQuery,serializeOpts:object=null):JSON{
        var opts={useIntKeysAsArrayIndex: true};
        if(serializeOpts!=null){
            _.extend(opts,serializeOpts)
        }
        let data = (form as any).serializeJSON(opts);
        return data;
    }

}
class ListPage extends WebConfig {
    protected grid: any;
    protected queryform: any;
    protected  queryBtn: any;
    protected listPageUrl: string;
    protected otherParams:object;

    constructor(grid: any, queryform: any, queryBtn: any, listPageUrl: string,otherParams:object={}) {
        super();
        this.grid = grid;
        this.queryform = queryform;
        this.queryBtn = queryBtn;
        this.listPageUrl = listPageUrl;
        this.otherParams=otherParams;
        (async ()=>{
            await this.init();
        })();
    }
    protected async resetButtons() {}
    get rows() {
        let rows = this.grid.bootstrapTable('getSelections');
        return rows;
    }
    private  async init(){
        this.refreshTableOptions();
        await this.resetButtons();
        this.queryBtn.click(async () => await this.queryGridData());
    }
    protected async queryGridData() {
        if (!this.queryform.validate().form()) {
            //@ts-ignore
            bs4pop.notice("请完善必填项", {type: 'warning', position: 'topleft'});
            return;
        }
        await this.resetButtons();
        this.grid.bootstrapTable('refresh');
    }
    protected removeAllDialog(){
        //@ts-ignore
        bs4pop.removeAll();
    }
    protected async alert(msg:string,cfg:any){
       await popwrapper.alert(msg,cfg);
    }

    // private get columns(){
    //     var cols=this.grid.bootstrapTable('getOptions')?.columns;
    //     if(_.isUndefined(cols)){
    //         return [];
    //     }
    //     return cols;
    //
    // }
    private buildMetaData(){
        //@ts-ignore
        let metadata=bui.util.bindGridMeta2Form(this.grid.attr('id'),this.queryform.attr('id')).metadata;
        if(_.isUndefined(metadata)){
            return {}
        }
        return metadata;
    }

    protected refreshTableOptions(){
        let url=this.toUrl(this.listPageUrl)
        let metadata=this.buildMetaData();

        let buildQueryData=(params)=>{
            let temp = {
                rows: params.limit,   //页面大小
                page: ((params.offset / params.limit) + 1) || 1, //页码
                sort: params.sort,
                order: params.order,
                metadata:metadata
            }
            $.extend(temp, this.queryform.serializeJSON());
            $.extend(temp, this.otherParams);
            let jsonData = jq.removeEmptyProperty(temp);

            return JSON.stringify(jsonData);
        }
        this.grid.bootstrapTable('refreshOptions', {
            url: url
            , 'queryParams': (params) => buildQueryData(params)
            ,'contentType':'application/json'
            , 'ajaxOptions': {contentType: 'application/json', dataType: 'json'}

        });
    }


}