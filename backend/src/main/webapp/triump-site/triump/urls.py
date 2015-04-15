from django.conf.urls import patterns, include, url
from django.conf import settings
from django.contrib import admin

admin.autodiscover()

urlpatterns = patterns('',
    
    url(r'^', include('base.urls', namespace="base")),
    url(r'^accounts/admin/', include(admin.site.urls)),
    url(r'^accounts/', include('accounts.urls', namespace="accounts")),
    url(r'^groups/', include('groups.urls', namespace="groups")),
    url(r'^events/', include('events.urls', namespace="events")),

)
if settings.DEBUG:
    urlpatterns += patterns('',
        (r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.MEDIA_ROOT})
    )
