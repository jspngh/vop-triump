from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse

@login_required()
def index(request):
    return render(request, 'events/index.html')

@login_required()
def details(request, event_id):
    return render(request, 'events/details.html', { 'event_id' : event_id })

@login_required()
def allevents(request):
    return HttpResponse("list of all groups")
