$(document).ready(function () {

    var $rateContainer = $(".rating"),
        $sel = $rateContainer.find("select"),
        $a = $rateContainer.find("a"),
        pId = $rateContainer.find("#rate-btn").attr("data-ref");

    $a.click(function () {
        $.ajax({
            type:"POST",
            url:"/vote/" + pId,
            data:{"score": $sel.val()},
            success: function () {
                alert("Vote successful!");
            },
            error : function () {
                alert('Vote unsuccessful, please retry!');
            }
        });
    });
});
