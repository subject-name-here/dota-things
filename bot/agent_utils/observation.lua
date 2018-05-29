-- Observation module
local Observation = {}

local bot = GetBot()

local ability1 = bot:GetAbilityByName('nevermore_shadowraze1')
local ability2 = bot:GetAbilityByName('nevermore_shadowraze2')
local ability3 = bot:GetAbilityByName('nevermore_shadowraze3')
local ability4 = bot:GetAbilityByName('nevermore_requiem')

-- Obtain team info.
local function get_team()
    if (GetTeam() == TEAM_RADIANT) then
        return 2
    else
        return 3
    end
end

function get_game_info()
    local game_info = {
        get_team(),
        bot:GetUnitName(),
    }
    return game_info
end

-- Obtain damage info.
function get_damage_info()
    local damage_info = {
        bot:TimeSinceDamagedByAnyHero(),
        bot:TimeSinceDamagedByCreep(),
        bot:TimeSinceDamagedByTower(),
    }
    return damage_info
end

-- Obtain towers info.
function get_towers_info()
    local enemy_tower = GetTower(TEAM_DIRE, TOWER_MID_1);
    local ally_tower = GetTower(TEAM_RADIANT, TOWER_MID_1);
    if get_team() == 3 then
        local temp = ally_tower
        ally_tower = enemy_tower
        enemy_tower = temp
    end

    return {
        enemy_tower:GetHealth(),
        ally_tower:GetHealth()
    }
end

--- Obtain bot's info (specified for Nevermore).
--
function get_self_info()
    local ability1_av = false
    if ability1:IsFullyCastable() then
        ability1_av = true
    end

    local ability2_av = false
    if ability2:IsFullyCastable() then
        ability2_av = true
    end

    local ability3_av = false
    if ability3:IsFullyCastable() then
        ability3_av = true
    end

    local ability4_av = false
    if ability4:IsFullyCastable() then
        ability4_av = true
    end

    -- Bot's atk, hp, mana, abilities, position x, position y
    local self_position = bot:GetLocation()
    local self_info = {
        bot:GetAttackDamage(),
        bot:GetLevel(),
        bot:GetGold(),
        bot:GetHealth(),
        bot:GetMaxHealth(),
        bot:GetMana(),
        bot:GetMaxMana(),
        bot:GetFacing(),
        ability1_av,
        ability2_av,
        ability3_av,
        ability4_av,
        self_position[1],
        self_position[2]
    }

    return self_info
end

-- Obtain enemy hero info.
function get_enemy_info()
    local enemy_hero_input = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }

    local enemy_table = GetUnitList(UNIT_LIST_ENEMY_HEROES)

    if enemy_table ~= nil and enemy_table[1] ~= nil then
        local enemy = enemy_table[1]
        local enemy_position = enemy:GetLocation()
        enemy_hero_input = {
            1,                        -- visibility flag
            enemy:GetAttackDamage(),
            enemy:GetLevel(),
            enemy:GetHealth(),
            enemy:GetMaxHealth(),
            enemy:GetMana(),
            enemy:GetMaxMana(),
            enemy:GetFacing(),
            enemy_position[1],
            enemy_position[2]
        }
    end

    return enemy_hero_input
end

-- Obtain creeps info.
function get_creeps_info(creeps)
    local creeps_info = {}

    for creep_key, creep in pairs(creeps)
    do
        local position = creep:GetLocation()
        table.insert(creeps_info, {
            -- TODO: get type of creeps
            creep:GetHealth(),
            creep:GetMaxHealth(),
            position[1],
            position[2]
        })
    end

    return creeps_info
end

-- Get whole observation.
function Observation.get_observation()
    local enemy_creeps = get_creeps_info(bot:GetNearbyCreeps(1500, true))
    local ally_creeps = get_creeps_info(bot:GetNearbyCreeps(1500, false))

    local observation = {
        ['game_info'] = get_game_info(),
        ['self_info'] = get_self_info(),
        ['enemy_info'] = get_enemy_info(),
        ['enemy_creeps_info'] = enemy_creeps,
        ['ally_creeps_info'] = ally_creeps,
        ['tower_info'] = get_towers_info(),
        ['damage_info'] = get_damage_info()
    }

    return observation
end

return Observation;
