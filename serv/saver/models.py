from django.db import models


class Action(models.Model):
    action_type = models.IntegerField()
    param = models.IntegerField()

    dx = models.IntegerField()
    dy = models.IntegerField()


class State(models.Model):
    our_team = models.IntegerField()
    enemy_hero = models.CharField(max_length=50)

    time = models.IntegerField()

    ###

    x = models.IntegerField()
    y = models.IntegerField()
    facing = models.FloatField()

    lvl = models.IntegerField()
    damage = models.IntegerField()
    gold = models.IntegerField()

    hp = models.IntegerField()
    max_hp = models.IntegerField()
    mana = models.IntegerField()
    max_mana = models.IntegerField()

    available1 = models.BooleanField()
    available2 = models.BooleanField()
    available3 = models.BooleanField()
    available4 = models.BooleanField()

    ###

    enemy_visible = models.BooleanField()

    enemy_x = models.IntegerField()
    enemy_y = models.IntegerField()
    enemy_facing = models.FloatField()

    enemy_lvl = models.IntegerField()
    enemy_damage = models.IntegerField()

    enemy_hp = models.IntegerField()
    enemy_max_hp = models.IntegerField()
    enemy_mana = models.IntegerField()
    enemy_max_mana = models.IntegerField()

    ###

    tower_hp = models.IntegerField()
    enemy_tower_hp = models.IntegerField()

    ###

    action_done = models.ForeignKey(Action, on_delete=models.CASCADE)


class CreepState(models.Model):
    state_host = models.ForeignKey(State, on_delete=models.CASCADE)

    x = models.IntegerField()
    y = models.IntegerField()

    hp = models.IntegerField()
    max_hp = models.IntegerField()

    type = models.IntegerField()
    num = models.IntegerField()
    isEnemy = models.BooleanField
