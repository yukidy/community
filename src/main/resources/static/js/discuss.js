
// $(function () {}); 与js中window.onload=function()意思一样，
// 表示页面加载事件，function函数在页面加载完之后调用
// 也可在html标签内直接声明单击事件click，再js文件中实现单击事件函数即可，如function like;
$(function () {
	// 页面加载完之后，绑定按键单击事件
	$("#topBtn").click(setTop);
	$("#wonderfulBtn").click(setWonderful);
	$("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId) {
	$.post(
		CONTEXT_PATH + "/like",
		{
			"entityType" : entityType,
			"entityId" : entityId,
			"entityUserId" : entityUserId,
			"postId" : postId
		},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				// 成功,改变页面赞数量和状态
				$(btn).children("i").text(data.likeCount);
				$(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
			} else {
				alert(data.msg);
			}
		}
	);
}

// 置顶
function setTop() {
	var postId = $("#adPostId").val();
	$.post(
		CONTEXT_PATH + "/discuss/top",
		{"postId" : postId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				// 设置当前按钮事件不可用
				$("#topBtn").attr("disabled", "disabled");
			} else {
				alert(data.msg);
			}
		}
	);
}

// 加精
function setWonderful() {
	var postId = $("#adPostId").val();
	$.post(
		CONTEXT_PATH + "/discuss/wonderful",
		{"postId" : postId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				// 设置当前按钮事件不可用
				$("#wonderfulBtn").attr("disabled", "disabled");
			} else {
				alert(data.msg);
			}
		}
	);
}

// 删除
function setDelete() {
	var postId = $("#adPostId").val();
	$.post(
		CONTEXT_PATH + "/discuss/delete",
		{"postId" : postId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				// 删帖成功，返回首页面
				location.href = CONTEXT_PATH + "/index";
			} else {
				alert(data.msg);
			}
		}
	);
}