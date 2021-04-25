/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.27.744 on 2021-04-25 15:53:23.

interface Market {
    id: number;
    code: string;
    name: string;
    appId: number;
    appSecret: string;
    contextUrl: string;
    platformMarketId: number;
}

declare const enum RegisterSourceEnum {
    OTHERS = 0,
    TALLY_AREA = 1,
    TRADE_AREA = 2,
}

declare const enum BillVerifyStatusEnum {
    WAIT_AUDIT = 0,
    RETURNED = 10,
    PASSED = 20,
    NO_PASSED = 30,
    DELETED = 40,
}

declare const enum DetectStatusEnum {
    NONE = 0,
    WAIT_DESIGNATED = 10,
    WAIT_SAMPLE = 20,
    WAIT_DETECT = 30,
    DETECTING = 40,
    FINISH_DETECT = 50,
    RETURN_DETECT = 60,
}

declare const enum DetectResultEnum {
    NONE = 0,
    PASSED = 1,
    FAILED = 2,
}

declare const enum DetectTypeEnum {
    NEW = 10,
    INITIAL_CHECK = 20,
    RECHECK = 30,
    SPOT_CHECK = 40,
    OTHERS = 9999,
}

declare const enum RegistTypeEnum {
    NONE = 10,
    SUPPLEMENT = 20,
    PARTIAL = 30,
}
