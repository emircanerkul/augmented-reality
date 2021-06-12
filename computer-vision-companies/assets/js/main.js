$(function () {

    var data;
    $.getJSON("data.json").done(function (json) {
        var countries = {};

        $.each(json, (i) => { countries[i] = "#fff"; });

        $('#vmap').vectorMap(
            {
                map: 'world_en',
                backgroundColor: '#a5bfdd',
                borderColor: '#fff',
                borderOpacity: 0.25,
                borderWidth: 1,
                color: '#f4f3f0',
                colors: countries,
                enableZoom: 0,
                showTooltip: 0,
                hoverColor: '#c9dfaf',
                hoverOpacity: null,
                normalizeFunction: 'linear',
                scaleColors: ['#b6d6ff', '#005ace'],
                selectedColor: '#f4f3f0',
                selectedRegions: 0,
                selectedRegions: 0,
                multiSelectRegion: 0,
                showTooltip: 1,
                onRegionClick: function (element, code, region) {
                    if ($.inArray(code, Object.keys(countries)) == -1) {
                        event.preventDefault();
                    }
                    else {
                        $(".list").html("");
                        $("#detail > .title").text("Computer Vision Companies in " + json[code].t);
                        $.each(json[code].data, (i, v) => {
                            $(".list").append($("<li></li>").append($("<a></a>").attr("target", "_blank").attr("href", v.url).text(v.title), $("<span></span>").addClass("desc").text(v.desc)));
                        });

                        if (!is_hide) {
                            switch_detail()
                            setTimeout(() => {
                                switch_detail();
                            }, 500);
                        } else
                            switch_detail();
                    }
                },
                onLabelShow: function (event, label, code) {

                    if ($.inArray(code, Object.keys(countries)) == -1) {
                        event.preventDefault();
                    }
                    else label.text(label.text() + " (" + json[code].data.length + ")");
                },
            });
    });
});

var is_hide = true;

function switch_detail() {
    var text = $('#detail').html();
    $('#detail').html("");
    width = (0.2 * $('#detail').parent().width()) + 'px';
    if (is_hide) {
        $('#main').animate({ 'width': '-=' + width }, {
            step: function (now, fx) {
                $("#vmap").trigger("resize");
            }
        });
        $('#detail').animate({ 'width': '+=' + width }, 400);
        is_hide = false;
    }
    else {
        $('#main').animate({ 'width': '+=' + width }, {
            step: function (now, fx) {
                $("#vmap").trigger("resize");
            }
        });
        $('#detail').animate({ 'width': '-=' + width }, 400);
        is_hide = true;
    }
    setTimeout(() => {
        $('#detail').html(text);
        $('#detail > *').not(".close").css("opacity", 0);
        $('#detail > .title').animate({ 'opacity': '+=1' }, 500, () => {
            $('#detail > .list').animate({ 'opacity': '+=1', 'margin-top': '25' }, 500);
        });
    }, 500);
}
