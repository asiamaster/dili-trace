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

            hello: () => {};
        };

        export class B {
        };
    }
}
export namespace demo {
    export class Test1 {
        constructor() {
            let a: abc.def.A = new abc.def.A();
            (async () => {
                await a.hello();
            })();
        }
    }

    export class Test2 {
        constructor() {
        }
    }

}

interface A {
    name?: string;
    age: number;
}

let a = <A>{name: "abc"};
a.age = 23;
a.name = "ddd";
console.info(a);

interface B {
    (a: number, b: number): boolean;
}

let b: B = (a1, b1) => {
    return a1 > b1;
}

interface C {
    [index: number]: string;
}

let c: C = ['a', 'b'];

let d: Array<string> = ['a', 'b'];
let e: [number] = [1];

let f: number[] = [1, 2, 3];
let g: [number, string] = [1, 'ss'];

interface E {
    name: string;
    age: number;
}

class ImplE implements E {
    readonly age: number;
    name: string;

    constructor() {
        this.age = 23;
        this.name = "abc";
    }
}

var e2 = new ImplE();
e2.name = "ss";

type sss = { name: string, age: number };
let ss: sss = {name: '', age: 34};

let n: { name: string, age: number } = {age: 23, name: ''};


// e2.age=44; error

import {Component} from 'vue'
var comp = Vue.component("demo", {
    props:['todo'],
    template: '<a href="">{{todo.text}}</a>',
});
var app=new Vue(
    {
        el:'',
        data:{},
    }
);

console.info("abcd")
