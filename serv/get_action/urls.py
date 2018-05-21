from django.conf.urls import url
from django.contrib import admin
from get_action import views


urlpatterns = [
    url(r'get_action.*?$', views.get_action, name='get_infos'),
]