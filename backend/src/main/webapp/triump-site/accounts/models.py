from django.db import models
from django.contrib.auth.models import User

class ExtendedUser(models.Model):
    user=models.OneToOneField(User)

    sessionToken = models.CharField(max_length=30, blank=True)
    fsUserId = models.CharField(max_length=30, blank=True)

    def __unicode__(self):
        return self.user.username