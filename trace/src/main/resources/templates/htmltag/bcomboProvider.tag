<% if(isNotEmpty(_escape) && _escape == "true") {%>
&lt;script&gt;
<% }else {%>
<script>
    <% }%>
    $(function () {
        //_logTable 动态标签元素计数器
        let $table = $('#${_id}').parents('[_logTable]');
        if(typeof(${_logVariable!'Log'}) !== 'undefined' && $table.length > 0){
            if(typeof(${_logVariable!'Log'}.tableItemTagCount) == 'undefined'){
                ${_logVariable!'Log'}.tableItemTagCount = 0;
            }
            ++${_logVariable!'Log'}.tableItemTagCount;
        }

        let option = $.extend(
            {${_option!}},
            <% if( isNotEmpty(_onLoadSuccess) ) {%>
            {onLoadSuccess : ${_onLoadSuccess!}}
        <% } else { %>
        {}
        <% } %>
    );
        var defaultValue='${_value!}';
        if(defaultValue&&defaultValue.trim()!=''){
            var defaultValueOptionHtml=template('optionItem', {
                selected: 'selected',
                value:defaultValue,
                text:''
            });
            let tempHtml=$(defaultValueOptionHtml).hide();
            $('#${_id}').append(tempHtml);
        }

        $.ajax($.extend(true,{
            <% if( isNotEmpty(_provider) ) {%>
            type: "post",
            url: '/provider/getLookupList.action',
            contentType:'application/json',
            data: {
                provider: '${_provider}',
                <% if( isNotEmpty(_queryParams) ) {%>
                queryParams: '${_queryParams!}'
                <% } %>
            },
            <% } %>
            dataType: "json",
            success: function (result) {
                let data;
                if(result instanceof Array){
                    data = result;
                }else if (typeof (result) == 'object') {
                    if(result.success){
                        data = result.data;
                    }else{
                        bs4pop.alert(result.message, {type: 'error'});
                        return;
                    }
                }

                $.map(data, function (dataItem) {
                    var tempHtml=template('optionItem', $.extend(dataItem, {
                        selected: '${_value!}' == dataItem.value + '',
                        value:dataItem["${_valueField!'value'}"],
                        text:dataItem["${_textField!'text'}"]
                    }));
                    if($('#${_id} option[value="'+dataItem.value+'"]').length==1){
                        $('#${_id} option[value="'+dataItem.value+'"]').remove();
                    }
                    $('#${_id}').append(tempHtml);
                });

                <% if( isNotEmpty(_log)) {%>
                if(typeof(${_logVariable!'Log'}) !== 'undefined'){
                    if($table.length > 0){//待所有logTable元素加载完执行日志搜集
                        if(--${_logVariable!'Log'}.tableItemTagCount == 0){
                            $.extend(${_logVariable!'Log'}.oldFields, ${_logVariable!'Log'}.buildTableFields($table));
                        }
                    }else{
                        $('#${_id}').attr('_log','${_log}');
                        if(typeof(${_logVariable!'Log'}) !== 'undefined'){
                            $.extend(${_logVariable!'Log'}.oldFields,Log.buildFields('#${_id}'));
                        }
                    }
                }
                <% } %>

                option.onLoadSuccess && option.onLoadSuccess(data);
            },
            error: function () {
                console.log('数据接口异常');
            }
        },option));
    })
    <% if(isNotEmpty(_escape) && _escape == "true") {%>
    &lt;/script&gt;
    <% }else {%>
</script>
<% }%>