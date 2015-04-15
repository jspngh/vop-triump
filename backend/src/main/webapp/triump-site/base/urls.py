from django.conf.urls import patterns, url
from base import views

urlpatterns = patterns('', 
	url(r'^form/$', views.form, name='form'),
    url(r'^about/$', views.about, name='about'),
    url(r'^contact/$', views.contact, name='contact'),
	url(r'^vote/', views.vote, name='vote'), 
	url(r'^$', views.index, name='index'),
)