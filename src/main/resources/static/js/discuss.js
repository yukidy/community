function like(btn, entityType, entityId) {

	$.post(
		CONTEXT_PATH + "/like",
		{"entityType" : entityType, "entityId" : entityId},
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