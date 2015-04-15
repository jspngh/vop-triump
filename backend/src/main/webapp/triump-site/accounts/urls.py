from django.conf.urls import patterns, url
from accounts import views

urlpatterns = patterns('',
	url(r'^$', views.index, name='index'),
    url(r'^register/$', views.register, name='register'),
	url(r'^login/$', views.login, name='login'),
    url(r'^logout/$', views.logout, name='logout'),
    url(r'^profile/$', views.profile, name='profile'),
    url(r'^foursquare/$', views.profile, name='fs_index'),
    url(r'^foursquare/auth/$', views.fs_auth, name='fs_auth'),
    url(r'^foursquare/callback/', views.fs_callback, name='fs_callback'),
    url(r'^foursquare/done/', views.fs_done, name='fs_done'),
)