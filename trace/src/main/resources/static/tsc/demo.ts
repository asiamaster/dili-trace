export class DemoClass {

    constructor(props) {
        // @ts-ignore
        $.makeArray("");
        _.chain([]).value();
        console.info("bcd")
    }
}

export namespace abc {
    export namespace def {
        export class A {
            constructor() {
            }
            hello: ()=>{};
        };

        export class B {
        };
    }
}
export namespace demo {
    export class Test1 {
        constructor() {
            let a: abc.def.A = new abc.def.A();
            (async ()=>{
               await a.hello();
            })();
        }
    }
    export class Test2 {
        constructor() {
        }
    }

}
console.info("abcd")
