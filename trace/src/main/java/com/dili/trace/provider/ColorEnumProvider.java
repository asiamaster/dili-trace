package com.dili.trace.provider;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.trace.glossary.UserQrStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author asa.lee
 */
@Component
public class ColorEnumProvider implements ValueProvider {

    private static final List<ValuePair<?>> BUFFER  = buildValuePair();

	private static List<ValuePair<?>> buildValuePair() {

		List<ValuePair<?>> list = new ArrayList<>();
		list.addAll(Stream.of(UserQrStatusEnum.values())
				.map(e -> new ValuePairImpl<>(e.getDesc(), e.getCode().toString())).collect(Collectors.toList()));
		return list;
	}
  

    @Override
    public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
        return BUFFER;
    }

    @Override
    public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
        if (null == object) {
            return null;
        }
        ValuePair<?> valuePair = BUFFER.stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElse(null);
        if (null != valuePair) {
            return valuePair.getText();
        }
        return null;
    }
}
