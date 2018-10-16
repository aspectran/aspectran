<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html class="no-js" lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
    <meta name="google" content="notranslate">
    <title>${empty page.title ? "Aspectran Demo Site" : page.title}</title>
    <meta name="description" content="${empty page.description ? "Welcome to Aspectran Demo" : page.description}" />
    <link rel="stylesheet" type="text/css" href="http://www.aspectran.com/assets/css/styles_aspectran.css" />
    <link href="http://fonts.googleapis.com/css?family=Raleway:500,500i,700" rel="stylesheet">
    <script src="http://www.aspectran.com/assets/js/modernizr.js"></script>
    <script src="http://www.aspectran.com/assets/js/jquery.js"></script>
    <script src="http://www.aspectran.com/assets/js/fastclick.js"></script>
    <link rel="mask-icon" href="http://www.aspectran.com/assets/img/aspectran-logo.svg" color="#4B555A" />
    <link rel="apple-touch-icon" sizes="57x57" href="http://www.aspectran.com/assets/img/apple-icon-57x57.png" />
    <link rel="apple-touch-icon" sizes="60x60" href="http://www.aspectran.com/assets/img/apple-icon-60x60.png" />
    <link rel="apple-touch-icon" sizes="72x72" href="http://www.aspectran.com/assets/img/apple-icon-72x72.png" />
    <link rel="apple-touch-icon" sizes="76x76" href="http://www.aspectran.com/assets/img/apple-icon-76x76.png" />
    <link rel="apple-touch-icon" sizes="114x114" href="http://www.aspectran.com/assets/img/apple-icon-114x114.png" />
    <link rel="apple-touch-icon" sizes="120x120" href="http://www.aspectran.com/assets/img/apple-icon-120x120.png" />
    <link rel="apple-touch-icon" sizes="144x144" href="http://www.aspectran.com/assets/img/apple-icon-144x144.png" />
    <link rel="apple-touch-icon" sizes="152x152" href="http://www.aspectran.com/assets/img/apple-icon-152x152.png" />
    <link rel="apple-touch-icon" sizes="180x180" href="http://www.aspectran.com/assets/img/apple-icon-180x180.png" />
    <link rel="icon" type="image/png" sizes="192x192"  href="http://www.aspectran.com/assets/img/android-icon-192x192.png" />
    <link rel="icon" type="image/png" sizes="16x16" href="http://www.aspectran.com/assets/img/favicon-16x16.png" />
    <link rel="icon" type="image/png" sizes="32x32" href="http://www.aspectran.com/assets/img/favicon-32x32.png" />
    <link rel="icon" type="image/png" sizes="96x96" href="http://www.aspectran.com/assets/img/favicon-96x96.png" />
    <meta name="msapplication-TileImage" content="http://www.aspectran.com/assets/img/ms-icon-144x144.png" />
    <meta name="msapplication-TileColor" content="#4B555A" />
    <meta name="theme-color" content="#4B555A" />
    <!-- Facebook Optimization -->
    <meta property="og:locale" content="en_US" />
    <meta property="og:type" content="website" />
    <meta property="og:title" content="Quick Start Guide" />
    <meta property="og:description" content="Aspectran is a Java framework for building Web and command-line applications." />
    <meta property="og:url" content="http://www.aspectran.com/getting-started/quickstart/" />
    <meta property="og:site_name" content="Aspectran" />
    <!-- Search Engine Optimization -->
    <link type="text/plain" rel="author" href="http://www.aspectran.com/humans.txt" />
    <script>
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
            (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
        ga('create', 'UA-66807210-2', 'auto');
        ga('set', 'anonymizeIp', true);
        ga('send', 'pageview');
    </script>
</head>
<body id="top-of-page" class="article" itemscope itemtype="http://schema.org/WebPage">
<nav id="navigation" class="no-js">
    <div class="title-bar" data-responsive-toggle="gnb-menu" data-hide-for="large" style="display:none">
        <div class="title-bar-left">
            <a class="logo" href="/" title="Aspectran"><img src="http://www.aspectran.com/assets/img/aspectran-icon.png" alt="Aspectran"/></a>
        </div>
        <div class="title-bar-center">
            <a href="#top-of-page">Aspectran</a>
        </div>
        <div class="title-bar-right" data-toggle>
            <a class="menu-icon" title="Menu"></a>
        </div>
    </div>
    <div class="top-bar" id="gnb-menu" style="display:none">
        <div class="row">
            <div class="top-bar-logo">
                <div class="circle">
                    <a class="logo" href="/" title="Aspectran"><img src="http://www.aspectran.com/assets/img/aspectran-icon.png" alt="Aspectran"/></a>
                </div>
            </div>
            <div class="top-bar-left">
                <ul class="dropdown menu" data-dropdown-menu data-close-on-click-inside="false">
                    <li>
                        <a href="/examples/hello-world">Examples</a>
                        <ul class="menu" data-submenu data-close-on-click-inside="false">
                            <li><a href="/examples/hello-world">Hello World</a></li>
                            <li><a href="/examples/gs-rest-service/">RESTful Web Service</a></li>
                            <li><a href="/examples/file-upload/">File Upload</a></li>
                        </ul>
                    </li>
                    <li>
                        <a href="/terminal/">Terminal</a>
                    </li>
                </ul>
            </div>
            <div class="top-bar-right">
                <ul class="dropdown menu" data-dropdown-menu data-close-on-click-inside="false">
                </ul>
                <div class="quick-search-box show-for-large">
                    <form name="google_quick_search">
                        <div class="input-group">
                            <input class="input-group-field" type="text" name="keyword" placeholder="Search">
                            <div class="input-group-button">
                                <button type="submit" class="button"><i class="fi-magnifying-glass"></i></button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class="row quick-search-box hide-for-large">
            <div class="small-12 columns">
                <form name="google_quick_search">
                    <div class="input-group">
                        <input class="input-group-field" type="text" name="keyword" placeholder="Search">
                        <div class="input-group-button">
                            <button type="submit" class="button">Search</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="breadcrumbs-bar" style="display:none" data-hide-for="medium down">
            <div class="row">
                <nav role="navigation" aria-label="You are here:">
                    <ul class="breadcrumbs" itemprop="breadcrumb">
                        <li><a href="http://www.aspectran.com/">Aspectran</a></li>
                        <li><a href="/">Demo</a></li>
                    </ul>
                </nav>
            </div>
        </div>
    </div>
</nav>
<section itemscope itemtype="http://schema.org/Article">
    <div id="masthead">
        <div id="forkme-ribbon" style="position: absolute; top: 0; right: 0; border: 0; width: 149px; height: 149px;">
            <a href="https://github.com/aspectran/aspectran" target="_blank">
                <img src="http://aral.github.com/fork-me-on-github-retina-ribbons/right-white@2x.png" width="149" height="149" alt="Fork me on GitHub"></a>
        </div>
        <div class="row">
            <div class="small-12 columns">
                <header>
                    <p class="subheadline" itemprop="alternativeHeadline">${page.subheadline}</p>
                    <h1 itemprop="headline">${page.headline}</h1>
                    <p class="teaser" itemprop="description">
                        ${page.teaser}
                    </p>
                </header>
                <div class="hexagons">
                    <div class="hexagon hex1"></div>
                    <div class="hexagon hex2"></div>
                    <div class="hexagon hex3"></div>
                    <div class="hexagon hex4"></div>
                    <div class="hexagon hex5"></div>
                    <div class="hexagon hex6"></div>
                </div>
            </div>
        </div>
        <div class="row breadcrumbs-bar">
            <div class="columns">
                <nav role="navigation" aria-label="You are here:">
                    <ul class="breadcrumbs" itemprop="breadcrumb">
                        <li><a href="http://www.aspectran.com/">Aspectran</a></li>
                        <li><a href="/">Demo</a></li>
                    </ul>
                </nav>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="large-12 columns">
            <c:if test="${not empty page.include}">
                <jsp:include page="/WEB-INF/jsp/${page.include}.jsp"/>
            </c:if>
        </div>
    </div>
</section>
<div id="up-to-top" class="row">
    <div class="small-12 columns" style="text-align: right;">
        <a class="iconfont" href="#top-of-page">&#xf108;</a>
    </div><!-- /.small-12.columns -->
</div><!-- /.row -->
<footer id="footer-content">
    <div id="footer">
        <div class="row">
            <div class="medium-2 large-1 columns hide-for-small-only t5">
                <h5><a class="logo" href="http://www.aspectran.com/info/" title="Aspectran"><img src="http://www.aspectran.com/assets/img/aspectran-logo-grey-x100.png" width="100" height="100" title="Aspectran"/></a></h5>
            </div>
            <div class="medium-4 large-4 columns">
                <a href="http://www.aspectran.com/info/"><h5>About Aspectran</h5></a>
                <p>
                    <a href="http://www.aspectran.com/info/">Aspectran is a Java framework for building Web and command-line applications.</a>
                </p>
            </div>
            <div class="small-6 medium-3 large-3 large-offset-1 columns">
                <h5>Navigation</h5>
                <ul class="no-bullet">
                    <li class="">
                        <a href="http://www.aspectran.com/getting-started/" title="">Getting-Started</a>
                    </li>
                    <li class="">
                        <a href="http://www.aspectran.com/docs/" title="">Documentation</a>
                    </li>
                    <li class="" >
                        <a href="http://www.aspectran.com/guides/" title="">Guides</a>
                    </li>
                    <li class="" >
                        <a href="http://www.aspectran.com/projects/" title="">Projects</a>
                    </li>
                </ul>
            </div>
            <div class="small-6 medium-3 large-3 columns">
                <h5>Services</h5>
                <ul class="no-bullet">
                    <li>
                        <a href="http://www.aspectran.com/support/" title="Aspectran Support">Support</a>
                    </li>
                    <li>
                        <a href="http://www.aspectran.com/feed.xml" title="Subscribe to RSS Feed">RSS</a>
                    </li>
                    <li>
                        <a href="http://www.aspectran.com/atom.xml" title="Subscribe to Atom Feed">Atom</a>
                    </li>
                    <li>
                        <a href="http://www.aspectran.com/sitemap.xml"  title="Sitemap for Google Webmaster Tools">sitemap.xml</a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <div id="subfooter">
        <nav class="row b30">
            <section id="subfooter-left" class="medium-6 columns credits">
                <p>© 2008–2018 The Aspectran Project. All rights reserved.</p>
            </section>
            <section id="subfooter-right" class="medium-6 columns social-icons">
                <ul>
                    <li><a href="http://github.com/topframe" target="_blank" class="icon-github" title="Code and more ..."></a></li>
                    <li><a href="http://twitter.com/aspectran" target="_blank" class="icon-twitter" title="Always the latest news from aspectran there on Twitter"></a></li>
                    <li><a href="http://www.facebook.com/aspectran" target="_blank" class="icon-facebook" title="Let's be friends!"></a></li>
                </ul>
            </section>
        </nav>
    </div>
</footer>
<script src="http://www.aspectran.com/assets/js/foundation.min.js"></script>
<script>
    var path = location.pathname;
    var a1 = $("#gnb-menu .top-bar-left .dropdown li a[href='" + path + "']").last();
    if (a1.size() > 0) {
        var arr = [];
        arr.push({'name': a1.text(), 'href': null});
        a1.parentsUntil(".dropdown > li:eq(0)").each(function() {
            if ($(this).hasClass("menu")) {
                var a2 = $(this).prev();
                if (a2.is("a")) {
                    arr.push({'name': a2.text(), 'href': a2.attr("href")||""});
                }
            }
        });
        arr.reverse();
        for (var i in arr) {
            var item = arr[i];
            if (i < arr.length - 1) {
                $(".breadcrumbs").append("<li><a href='" + item.href + "'>" + item.name + "</a></li>");
            } else {
                $(".breadcrumbs").append("<li><span class='show-for-sr'>Current: </span> <span class='current'>" + item.name + "</span></li>");
            }
        }
    }
    $(document).foundation();
    $(document).ready(function() {
        var $win = $(window);
        var $nav = $("#navigation");
        var navHeight = $("#masthead").height() - $nav.height();
        var lastScrollTop = 0;
        var scrolled;
        var navFixed;
        $win.scroll(function() {
            scrolled = true;
        });
        setInterval(function() {
            if (scrolled) {
                var scrollTop = $win.scrollTop();
                if (Math.abs(lastScrollTop - scrollTop) <= 10) {
                    return;
                }
                if (scrollTop <= navHeight) {
                    if (navFixed) {
                        $nav.removeClass("fixed");
                        navFixed = false;
                    }
                } else if (scrollTop > lastScrollTop) {
                    if (navFixed) {
                        $nav.removeClass("fixed");
                        navFixed = false;
                    }
                } else {
                    if (!navFixed) {
                        $nav.addClass("fixed");
                        $nav.hide().fadeIn(500);
                        navFixed = true;
                    }
                }
                lastScrollTop = scrollTop;
                scrolled = false;
            }
        }, 200);
        /* google search */
        $("form[name=google_quick_search]").submit(function(event) {
            window.open('https://www.google.com/search?q=' + this.keyword.value + '+site:http%3A%2F%2F0.0.0.0%3A4000');
            event.preventDefault();
        });
    });
</script>
<script>
    $(document).ready(function() {
        $("#masthead h1, article h1, article h2, article h3, article h4, article h5, article h6").each(function(index, item) {
            var tagn = item.localName;
            var anchor = "top-of-page";
            if(tagn != "h1") {
                anchor = "anchor-" + (index + 1);
                $(this).before("<a class='toc-anchor " + anchor + "' id='" + anchor + "' name='" + anchor + "'></a>");
            }
            $("#toc ul").append("<li class='toc-" + tagn + "'><a anchor='" + anchor + "' href='#" + anchor + "'>" + $(item).text() + "</a></li>");
        });
    });
</script>
<script>
    $(document).ready(function() {
        $(".lazy-sticky").each(function() {
            var $win = $(window);
            var $this = $(this);
            var topNavHeight = 60;
            var upToTopHeight = $("#up-to-top").height() + 30 + 60;
            var footerHeight = $("#footer-content").height() + upToTopHeight;
            var baseOffsetTop = $this.offset().top - topNavHeight;
            var offsetTop = 0;
            var thisHeight = $this.height();
            var winHeight = $win.height();
            var scrollTimer = null;
            var immediate = false;
            $this.find("#toc ul a").click(function(e) {
                immediate = true;
                var anchor = $(this).attr("anchor");
                if(anchor != "top-of-page") {
                    setTimeout(function() {
                        var offset = $("#" + anchor).offset();
                        if(offset) {
                            immediate = true;
                            $win.scrollTop(offset.top - topNavHeight);
                        }
                    }, 100);
                }
            });
            $win.scroll(function() {
                var scrollTop = $win.scrollTop();
                if(scrollTop < baseOffsetTop) {
                    if(scrollTimer) {
                        clearInterval(scrollTimer);
                        scrollTimer = null;
                    }
                    scrollTimer = setInterval(function() {
                        if(offsetTop != 0) {
                            $this.css({
                                top: 0
                            });
                        }
                        offsetTop = 0;
                        clearInterval(scrollTimer);
                        scrollTimer = null;
                        immediate = false;
                    }, immediate ? 250 : 500);
                } else {
                    if(immediate || (scrollTop > baseOffsetTop + offsetTop + thisHeight - 20) || (scrollTop < baseOffsetTop + offsetTop)) {
                        var tocOffsetLeftBase = $this.offset().left;
                        if(tocOffsetLeftBase > 100) {
                            if(scrollTimer) {
                                clearInterval(scrollTimer);
                                scrollTimer = null;
                            }
                            scrollTimer = setInterval(function() {
                                scrollTop = $win.scrollTop();
                                if(scrollTop < baseOffsetTop) {
                                    scrollTop = 0;
                                } else {
                                    scrollTop = scrollTop - baseOffsetTop + 10;
                                }
                                if(scrollTop > $(document).height() - footerHeight - thisHeight - baseOffsetTop - topNavHeight) {
                                    scrollTop = $(document).height() - footerHeight - thisHeight - baseOffsetTop - topNavHeight;
                                }
                                offsetTop = scrollTop;
                                $this.css({
                                    position: "relative"
                                });
                                $this.animate({
                                    top: scrollTop + "px"
                                }, 300);
                                clearInterval(scrollTimer);
                                scrollTimer = null;
                                winHeight = $win.height();
                                thisHeight = $this.height();
                                footerHeight = $("#footer-content").height() + upToTopHeight;
                                immediate = false;
                            }, immediate ? 250 : 500);
                        }
                    }
                }
            });
            $win.resize(function() {
                var tocOffsetLeftBase = $this.offset().left;
                if(tocOffsetLeftBase <= 100) {
                    clearInterval(scrollTimer);
                    $this.css("top", 0);
                } else {
                    $win.scroll();
                }
            });
            setTimeout(function() {
                if($win.scrollTop() > baseOffsetTop) {
                    offsetTop = $win.scrollTop();
                    $win.scroll();
                }
            }, 150);
        });
    });
</script>
<script>
    /* Creating custom :external selector */
    $.expr[':'].external = function(obj) {
        return !obj.href.match(/^javascript\:/)
            && !obj.href.match(/^mailto\:/)
            && (obj.hostname != location.hostname);
    };
    $(function(){
        /* Add 'external' CSS class to all external links */
        $('a:external').addClass('external');
        /* turn target into target=_blank for elements w external class */
        $(".external").attr('target','_blank');
    })
</script>
</body>
</html>