package com.dili.trace.provider;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.trace.enums.DetectResultEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DetectResultEnumProvider implements ValueProvider {


    private List<ValuePair<?>> buildValuePair() {

        List<ValuePair<?>> list = new ArrayList<>();
        list.addAll(Stream.of(DetectResultEnum.values())
                .map(e -> new ValuePairImpl<>(e.getName(), e.getCode().toString())).collect(Collectors.toList()));
        return list;
    }


    @Override
    public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
        return this.buildValuePair();
    }

    @Override
    public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
        if (null == object) {
            return null;
        }
        ValuePair<?> valuePair = this.buildValuePair().stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElse(null);
        if (null != valuePair) {
            return valuePair.getText();
        }
        return null;
    }
}
