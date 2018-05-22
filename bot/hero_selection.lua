-- hero_selection.lua

----------------------------------------------------------------------------------------------------

local MyBots = {
  "npc_dota_hero_pudge",
  "npc_dota_hero_pudge",
  "npc_dota_hero_nevermore",
  "npc_dota_hero_pudge",
  "npc_dota_hero_pudge"
};

function Think()
  local IDs = GetTeamPlayers(GetTeam());
  for i,id in pairs(IDs) do
    if IsPlayerBot(id) then
      SelectHero(id, MyBots[i]);
    end
  end

end


----------------------------------------------------------------------------------------------------
