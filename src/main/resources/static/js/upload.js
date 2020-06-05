function ossUpload() {
    $.ajax({
        url : CONTEXT_PATH + "/user/policy",
        type : "get",
        success : function (data) {
            var data = $.parseJSON(data);
            var host = data.host;
            var accessid = data.accessid;
            var policy = data.policy;
            var signature = data.signature;
            var dir = data.dir;
            var expire = data.expire;
            var filename = data.filename;

            var request = new FormData($("#uploadForm")[0]);
            request.append("OSSAccessKeyId", accessid); //Bucket 拥有者的Access Key Id。
            request.append("policy", policy); //policy规定了请求的表单域的合法性
            request.append("Signature", signature); //根据Access Key Secret和policy计算的签名信息，OSS验证该签名信息从而验证该Post请求的合法性
            //---以上都是阿里的认证策略
            var object_name = dir + filename + get_suffix(request.get("headerImage").name);

            request.append("key", object_name); //文件名字，可设置路径
            request.append("success_action_status", '200'); // 让服务端返回200,不然，默认会返回204
            request.append('file', request.get("headerImage")); //需要上传的文件 file

            // 正式上传
            $.ajax({
                url: host, //上传阿里地址
                data: request,
                processData: false, //默认true，设置为 false，不需要进行序列化处理
                cache: false, //设置为false将不会从浏览器缓存中加载请求信息
                async: false, //发送同步请求
                contentType: false, //避免服务器不能正常解析文件---------具体的可以查下这些参数的含义
                dataType: 'xml', //不涉及跨域  写json即可
                type: 'post',
                success: function(data) {
                    var headerUrl = host + "/" + object_name;
                    $.post(
                        CONTEXT_PATH + "/user/loadUrl",
                        {"headerUrl" : headerUrl},
                        function (data) {
                            var data = $.parseJSON(data);
                            if (data.code == 0) {
                                window.location.reload();
                            } else {
                                alert(data.msg);
                            }
                        }
                    );
                },
                error: function (returndata) {
                    alert("上传图片出错!");
                }
            });
        }
    });

}

function get_suffix(filename) {
    pos = filename.lastIndexOf('.');
    suffix = '';
    if (pos != -1) {
        suffix = filename.substring(pos);
    }
    console.log(suffix);
    return suffix;
}