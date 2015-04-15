from django.contrib.auth.models import User
from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.conf import settings
from django.http import HttpResponseRedirect, HttpResponse
from django.template import RequestContext
from django.template.response import TemplateResponse
from django.utils.http import is_safe_url
from django.utils.translation import ugettext as _
from django.shortcuts import resolve_url, render_to_response
from django.views.decorators.debug import sensitive_post_parameters
from django.views.decorators.cache import never_cache
from django.views.decorators.csrf import csrf_protect

import urllib
import urllib2
import json
from django.core.urlresolvers import reverse

# Avoid shadowing the login() and logout() views below.
from django.contrib.auth import (REDIRECT_FIELD_NAME, login as auth_login,
    logout as auth_logout)
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.sites.shortcuts import get_current_site

from triump.settings import FS_CLIENT_ID, FS_CLIENT_SECRET, FS_ACCESS_TOKEN_URL, FS_REDIRECT_URL, FS_TOKEN_REQUEST_URL
from accounts.forms import UserForm
from accounts.models import ExtendedUser

# Create your views here.
def index(request):
    return render(request, 'index.html')


@sensitive_post_parameters()
@csrf_protect
@never_cache
def register(request):
    if request.method == 'POST':
        user_form = UserForm(data=request.POST)
        if user_form.is_valid():
            user = user_form.save()
            user.set_password(user.password)
            user.save()
            Xuser = ExtendedUser.objects.create(user=user, sessionToken="", fsUserId="")
            return render(request, 'registration/completed.html')
        else:
            context = {'form': user_form}
            user_form.add_error(None, 'An error occured, please try again')
            return TemplateResponse(request, 'registration/register.html', context)

    else:
        user_form = UserForm()
        context = {'form': user_form}
        return TemplateResponse(request, 'registration/register.html', context)


@sensitive_post_parameters()
@csrf_protect
@never_cache
def login(request, template_name='registration/login.html',
          redirect_field_name=REDIRECT_FIELD_NAME,
          authentication_form=AuthenticationForm,
          current_app=None, extra_context=None):
    """
    Displays the login form and handles the login action.
    """
    redirect_to = request.POST.get(redirect_field_name,
                                   request.GET.get(redirect_field_name, ''))

    if request.method == "POST":
        form = authentication_form(request, data=request.POST)
        if form.is_valid():

            # Ensure the user-originating redirection url is safe.
            if not is_safe_url(url=redirect_to, host=request.get_host()):
                redirect_to = resolve_url(settings.LOGIN_REDIRECT_URL)

            # Okay, security check complete. Log the user in.
            auth_login(request, form.get_user())
            Xuser = ExtendedUser.objects.get(user = request.user)
            if not Xuser.sessionToken == "":
                return HttpResponseRedirect(redirect_to)
            else:
                request.session['redirect_to'] = redirect_to
                return HttpResponseRedirect('/accounts/foursquare/auth/')
    else:
        form = authentication_form(request)

    current_site = get_current_site(request)

    context = {
        'form': form,
        redirect_field_name: redirect_to,
        'site': current_site,
        'site_name': current_site.name,
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


def logout(request, next_page=None,
           template_name='registration/logged_out.html',
           redirect_field_name=REDIRECT_FIELD_NAME,
           current_app=None, extra_context=None):
    """
    Logs out the user and displays 'You are logged out' message.
    """
    auth_logout(request)

    if next_page is not None:
        next_page = resolve_url(next_page)

    if (redirect_field_name in request.POST or
            redirect_field_name in request.GET):
        next_page = request.POST.get(redirect_field_name,
                                     request.GET.get(redirect_field_name))
        # Security check -- don't allow redirection to a different host.
        if not is_safe_url(url=next_page, host=request.get_host()):
            next_page = request.path

    if next_page:
        # Redirect to this page until the session has been cleared.
        return HttpResponseRedirect(next_page)

    current_site = get_current_site(request)
    context = {
        'site': current_site,
        'site_name': current_site.name,
        'title': _('Logged out')
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
        current_app=current_app)

@login_required
def profile(request):
    Xuser = ExtendedUser.objects.get(user = request.user)
    if Xuser is not None:
        request.session['authToken'] = Xuser.sessionToken
        return render(request, 'profile.html', { 'sessionToken': request.session.get('authToken'), 'username': request.user.username })
    return HttpResponse("you are logged in: " + request.user.username)

@login_required
def fs_auth(request, redirect_to='/accounts/profile/'):
    params = {'client_id' : FS_CLIENT_ID,
            'response_type' : 'code',
            'redirect_uri' : FS_REDIRECT_URL }
    data = urllib.urlencode( params )
    # redirect the user to the url to confirm access for the app
    return HttpResponseRedirect('%s?%s' % (FS_TOKEN_REQUEST_URL, data))

@login_required
def fs_callback(request):
    code = request.GET.get('code')
    # build the url to request the access_token
    params = { 'client_id' : FS_CLIENT_ID,
    'client_secret' : FS_CLIENT_SECRET,
    'grant_type' : 'authorization_code',
    'redirect_uri' : FS_REDIRECT_URL,
    'code' : code}
    data = urllib.urlencode( params )
    binary_data = data.encode('ascii')
    req = urllib2.Request(FS_ACCESS_TOKEN_URL, binary_data)
    # request the access_token
    response = urllib2.urlopen( req )
    access_token = json.loads(response.read().decode('ascii'))
    access_token = access_token['access_token']
    # store the access_token for later use
    request.session['access_token'] = access_token
    # redirect the user to show we're done
    return HttpResponseRedirect(reverse('accounts:fs_done'))

@login_required
def fs_done(request):
    access_token = request.session.get('access_token')
    redirect_to = request.session.get('redirect_to')

    # request user details from foursquare
    params = {'oauth_token' : access_token,
              'v' : 20150402 }
    data = urllib.urlencode( params )
    url = 'https://api.foursquare.com/v2/users/self'
    full_url = url + '?' + data
    response = urllib2.urlopen(full_url)
    response = response.read().decode('utf-8')
    user = json.loads(response)['response']['user']
    userId = user['id']

    url = 'https://mystic-impulse-87918.appspot.com/_ah/api/myApi/v1/authtokenresponse/'+userId+'/'+access_token
    response = urllib2.urlopen(url)
    response = response.read().decode('utf-8')
    authToken = json.loads(response)['authToken']
    request.session['authToken'] = authToken

    Xuser = ExtendedUser.objects.get(user = request.user)
    Xuser.fsUserId = userId
    Xuser.sessionToken = authToken
    Xuser.save()

    return HttpResponseRedirect(reverse('accounts:profile'))
