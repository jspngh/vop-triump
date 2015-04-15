from django.conf.urls import patterns, url
from events import views

urlpatterns = patterns('',
	url(r'^(?P<event_id>[0-9|a-z|A-Z]+)/$', views.details, name='details'),
    url(r'^all/$', views.allevents, name='all'),
	url(r'^$', views.index, name='index'),
)