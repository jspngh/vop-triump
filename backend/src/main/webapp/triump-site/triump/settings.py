# -*- coding: utf-8 -*-
# Django settings for triump project.

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
import os
BASE_DIR = os.path.dirname(os.path.dirname(__file__))
TEMPLATE_DIRS = [os.path.join(BASE_DIR, 'templates')]
LOGIN_URL = '/accounts/login/'
LOGIN_REDIRECT_URL = '/accounts/profile/'

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '3d0jyjo+w+2kc#0j6uv$=@j&amp;&amp;%m$gtz5augiok0&amp;sg%20cm=-2'

ALLOWED_HOSTS = '*'

SITE_ID = 1

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True
TEMPLATE_DEBUG = DEBUG

TEST_RUNNER = 'django.test.runner.DiscoverRunner'

SERVER_EMAIL = 'info@wservices.ch'
DEFAULT_FROM_EMAIL = 'jonah.spanoghe@gmail.com'

ADMINS = (
    (u'Jonas Spanoghe', 'jonah.spanoghe@gmail.com'),
)

MANAGERS = ADMINS

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2',
        'NAME': 'triump_triump',
        'USER': 'triump',
        'PASSWORD': '7BVVph4WPtEJ',
        'HOST': '',
        'PORT': '',
    }
}

# Internationalization
# https://docs.djangoproject.com/en/dev/topics/i18n/
LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'UTC'
USE_I18N = True
USE_L10N = True
USE_TZ = True

# Absolute path to the directory that holds media.
# Example: "/home/username/projectname/media/"
MEDIA_ROOT = BASE_DIR+'/media/'

# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash if there is a path component (optional in other cases).
# Examples: "http://media.lawrence.com", "http://example.com/media/"
MEDIA_URL = '/media/'

# The absolute path to the directory where collectstatic will collect static files for deployment.
# Example: "/home/username/projectname/static/"
STATIC_ROOT = BASE_DIR+'/static/'

# URL to use when referring to static files located in STATIC_ROOT.
# Examples: "/static/", "http://static.example.com/"
STATIC_URL = '/static/'

INSTALLED_APPS = (
    'base',
    'accounts',
    'groups',
    'events',
    'django.contrib.admin',
    'django.contrib.admindocs',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.staticfiles',
)

MIDDLEWARE_CLASSES = (
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
)

# List of callables that know how to import templates from various sources.
TEMPLATE_LOADERS = (
#    ('django.template.loaders.cached.Loader', (
        'django.template.loaders.filesystem.Loader',
        'django.template.loaders.app_directories.Loader',
#    )),
)

ROOT_URLCONF = 'triump.urls'

WSGI_APPLICATION = 'triump.wsgi.application'

# Override the server-derived value of SCRIPT_NAME 
# See http://code.djangoproject.com/wiki/BackwardsIncompatibleChanges#lighttpdfastcgiandothers
FORCE_SCRIPT_NAME = ''

STATIC_URL = '/static/'
FS_CLIENT_ID = 'NIJYYHF55AGYZXT3Z4BHYKTHPRM0S405KTTF2ZPO53KS2BOX'
FS_CLIENT_SECRET = 'P1FBMC04YAOCVYVKO0FOSYGZVIJH21NOSAQNWVW4HU5BMGNG'
FS_REDIRECT_URL = 'http://www.triump.be/accounts/foursquare/callback/'
FS_TOKEN_REQUEST_URL = 'https://foursquare.com/oauth2/authenticate'
FS_ACCESS_TOKEN_URL = 'https://foursquare.com/oauth2/access_token'
