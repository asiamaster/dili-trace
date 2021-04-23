class UpStreamController extends WebConfig {

    public async listCities(upStreamQuery: any): Promise<any> {

        var url = await super.toUrl("/upStream/listByKeyWord.action");
        let resp = await jq.postJson(url, upStreamQuery);
        if (resp.code == '200') {
            return resp.data;
        } else {
            throw new Error(resp.message);
        }
    }
    public async save(upStreamDto: any): Promise<any> {

        var url = await super.toUrl("/upStream/save.action");
        let resp = await jq.postJson(url, upStreamDto);
        if (resp.code == '200') {
            return resp.data;
        } else {
            throw new Error(resp.message);
        }
    }


    public async listSuggestionsUpStreams(upStreamQuery: any): Promise<any> {
        try{
            let data = await this.listCities(upStreamQuery);

            return _.chain(data).map(item => {
                return {"id": item.id, "value": item.name,"item":item};
            }).value();
        }catch (e){
            console.error(e);
            return [];
        }

    }
    public lookupUpStreams(keyword:string,userId:number,done:Function){
        (async ()=>{
            let data=await this.listSuggestionsUpStreams({'keyword':keyword,userId:userId});
            var result={'suggestions':data};
            done(result)
        })();
    }

}