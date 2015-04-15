from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse

@login_required()
def index(request):
    if request.session.get('authToken') is not None:
        return render(request, 'groups/index.html', { 'sessionToken': request.session.get('authToken') })
    return HttpResponse("Something went wrong...")

@login_required()
def details(request, group_id):
    if request.session.get('authToken') is not None:
        return render(request, 'groups/details.html', { 'group_id' : group_id, 'sessionToken': request.session.get('authToken') })
    return HttpResponse("Something went wrong...")


def allgroups(request):
    if request.session.get('authToken') is not None:
        return render(request, 'groups/all.html', {'sessionToken': request.session.get('authToken')})
    return HttpResponse("Something went wrong...")

