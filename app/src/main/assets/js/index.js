$(document).on('click', "button.next", function(e) {
	if (!$("input[name='ip']").val()) {
		showEmptyTargetWord($("input[name='ip']"))
		return
	}
	if (!$("input[name='port']").val()) {
		showEmptyTargetWord($("input[name='port']"))
		return
	}
	// 重置
	$(".process1").hide()
	$(".formList.process2").css("display", "flex")
	$("button.process2").show()
	$("body .emptyTip").remove()
	if($("#court")[0].options.length == 1){
	    // 获取法院
        var ip = $("#ip").val();
        var port = $("#port").val();
        var context = $("#context").val();
        $.ajax({
            url: "http://"+ ip +":"+ port + context +"/board/querycourtlist",
            dataType: "json",
            type: "get",
            success: function (resp) {
                if (!resp || resp.result == '96') {
                    showEmptyTargetWord($("select[name='court']"),"系统异常,获取法院失败");
                    return;
                }
                if (resp.result == '6') {
                    showEmptyTargetWord($("select[name='court']"),"法院列表为空");
                    return;
                }
                // 法院数据
                var courtHTML = '<option value="">---</option>';
                var courtList = resp.object;
                $.each(courtList, function(index, item){
                     courtHTML += '<option value="'+ item.orgId + '" title="'+ item.orgName +'">'+ item.orgName +'</option>';
                });
                $('#court').html(courtHTML);
            },
            error: function(e) {
                showEmptyTargetWord($("select[name='court']"),"网络异常,获取法院失败");
                console.error("网络异常,获取法院失败",e);
            }
        });
        // 场地列表
        $('#court').change(function(){
            $("body .emptyTip").remove();
            $('#org').html('<option value="">---</option>');
            var courtId = $(this).val();
            $.ajax({
                url: "http://"+ ip +":"+ port + context +"/board/queryorglist",
                dataType: "json",
                type: "post",
                data: {
                    "courtId":courtId
                },
                success: function (resp) {
                    if (!resp || resp.result == '96') {
                        showEmptyTargetWord($("select[name='org']"),"系统异常,获取场地失败");
                        return;
                    }
                    if (resp.result == '6') {
                        showEmptyTargetWord($("select[name='org']"),"场地列表为空");
                        return;
                    }
                    // 法庭数据
                    var orgList = resp.object;
                    var orgHTML = '<option value="">---</option>';
                    $.each(orgList, function(index, item){
                         orgHTML += '<option value="'+ item.orgId + '" title="'+ item.orgName +'">'+ item.orgName +'</option>';
                    });
                    $('#org').html(orgHTML);
                },
                error: function(e) {
                    showEmptyTargetWord($("select[name='org']"),"网络异常,获取场地失败");
                    console.error("网络异常,获取场地失败",e);
                }
            })
        });
	}
})

$(document).on('click', "button.prev", function(e) {
	$(".formList.process1").css("display", "flex")
	$("button.process1").show()
	$(".process2").hide()
	$("body .emptyTip").remove();
})

$(document).on('click', "button.sumbit", function(e) {
	if (!$("select[name='court']").val()) {
		showEmptyTargetWord($("select[name='court']"))
		return
	}
	if (!$("select[name='org']").val()) {
		showEmptyTargetWord($("select[name='org']"))
		return
	}
	$("body .emptyTip").remove();
	// 将公告屏首页地址，存入本地存储
	var ip = $("#ip").val();
    var port = $("#port").val();
    var orgId = $("#org").val();
	var context = $("#context").val();
    var baseUrl = "http://"+ ip +":"+ port + context;
    localStorage.setItem("baseUrl", baseUrl);
    localStorage.setItem("orgId", orgId);
    window.location = baseUrl + "/amt/index?orgId="+ orgId;
})

function showEmptyTargetWord(target, errorMsg) {
	$("body .emptyTip").remove()
	let dom = target[0]
	let left = dom.getBoundingClientRect().x - 90
	let top = dom.getBoundingClientRect().bottom + 10
	let style = "left:" + left +"px; top:" + top + "px;"
	var msg = errorMsg == undefined?target.attr("data-error"):errorMsg;
	let errorTip = "<p class='emptyTip' style='" + style + "'>" + msg + "</p>"
	$("body").append(errorTip)
}


$(function(){
	// 判断本地存储是否经设置过IP/场地等信息，是的话直接跳转到公告屏首页
    var baseUrl = localStorage.getItem("baseUrl");
    var orgId = localStorage.getItem("orgId");
    if(baseUrl && orgId){
	    $(".login-container").hide();
	    setInterval(function(){
		   if(navigator.onLine){
			  window.location = baseUrl + "/amt/index?orgId="+ orgId;
		   }
	    }, 1000);
		return;
    }
})