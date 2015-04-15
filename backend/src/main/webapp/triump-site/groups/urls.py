from django.conf.urls import patterns, url
from groups import views

urlpatterns = patterns('',
	url(r'^(?P<group_id>[0-9]+)/$', views.details, name='details'),
    url(r'^all/$', views.allgroups, name='all'),
	url(r'^$', views.index, name='index'),
)