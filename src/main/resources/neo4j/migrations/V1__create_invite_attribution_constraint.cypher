CREATE CONSTRAINT invite_attribution_id_unique IF NOT EXISTS
FOR (attribution:InviteAttribution)
REQUIRE attribution.id IS UNIQUE;
