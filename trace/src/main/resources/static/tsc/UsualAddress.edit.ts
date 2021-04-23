class UsualAddressEdit extends WebConfig {
    dataForm: any;
    submitBtn: any;
    pw:Window;

    constructor(dataForm: any, submitBtn: any) {
        super();
        this.dataForm = dataForm;
        this.submitBtn = submitBtn;
        this.pw=window.parent.window;

        let cityController: CityController = new CityController();
        super.initTraceAutoComplete(this.dataForm.find('#addressInput'),
            function (query, done) {
                cityController.lookupCities(query, done)
            }, function (sug) {
                $(this).val(sug.value);
                $('input[name="addressId"]').val(sug.id);
                $('input[name="mergedAddress"]').val(sug.item.mergerName);
                $('input[name="address"]').val(sug.item.name);
            });

        this.submitBtn.on('click',async ()=>await  this.ajaxSubmit());

    }

    private async ajaxSubmit() {
         if (!this.dataForm.validate().form()) {
             //@ts-ignore
             bs4pop.notice("请完善必填项", {type: 'warning', position: 'topleft'});
             return;
         }

        var data=jq.removeEmptyProperty(this.dataForm.serializeJSON());
        let url=this.toUrl("/usualAddress/insert.action");
        let resp =   await jq.postJsonWithProcessing(url, data);
        if(!resp.success){
            //@ts-ignore
            bs4pop.notice(resp.message, {type: 'warning', position: 'topleft'});
            return;
        }
        p.call("editSuccess");
    }


}