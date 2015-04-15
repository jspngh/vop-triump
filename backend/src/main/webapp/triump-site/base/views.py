from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse

def index(request):
    return render(request, 'home/index.html')

def about(request):
    return render(request, 'home/about.html')

def contact(request):
    return render(request, 'home/contact.html')
	
@login_required()
def form(request):
    return render(request, 'home/form.html')
def vote(request):
    try:
        color = request.POST['choice']
    except (KeyError):
        return render(request, 'home/form.html', {
						'error_message': "You didn't select a choice.",
					})
    return HttpResponse("Vote received: " + color)