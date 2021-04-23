class BrandController extends WebConfig {

    public async listBrands(brandQuery: any): Promise<any> {

        var url = await super.toUrl("/brand/listBrands.action");
        let resp = await jq.postJson(url, brandQuery);
        if (resp.code == '200') {
            return resp.data;
        } else {
            throw new Error(resp.message);
        }
    }



    public async listSuggestionsUpStreams(brandQuery: any): Promise<any> {
        try{
            let data = await this.listBrands(brandQuery);

            return _.chain(data).map(item => {
                return {"id": item.id, "value": item.name,"item":item};
            }).value();
        }catch (e){
            console.error(e);
            return [];
        }

    }
    public lookupBrands(likeBrandName:string,done:Function){
        (async ()=>{
            let data=await this.listSuggestionsUpStreams({'likeBrandName':likeBrandName});
            var result={'suggestions':data};
            done(result)
        })();
    }

}