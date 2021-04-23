class RegisterHeadController extends WebConfig {

    public async listRegisterHead(registerHeadQuery: any): Promise<any> {

        var url = await super.toUrl("/registerHead/listRegisterHead.action");
        let resp = await jq.postJson(url, registerHeadQuery);
        if (resp.code == '200') {
            return resp.data;
        } else {
            throw new Error(resp.message);
        }
    }

    public async listSuggestionsRegisterHeads(registerHeadQuery: any): Promise<any> {
        try{
            let data = await this.listRegisterHead(registerHeadQuery);

            return _.chain(data).map(item => {
                return {"id": item.id, "value": item.mergerName,"item":item};
            }).value();
        }catch (e){
            console.error(e);
            return [];
        }

    }
    public lookupRegisterHead(likeProductName:string,done:Function){
        (async ()=>{
            let data=await this.listRegisterHead({'likeProductName':likeProductName});
            var result={'suggestions':data};
            done(result)
        })();
    }

}