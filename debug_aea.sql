SELECT * FROM GroupMember;

SELECT * FROM UserGroup;

DROP TABLE GroupMember;

DROP TABLE UserGroup;

DELETE FROM UserGroup;

SELECT g FROM Group as g WHERE g.validated = TRUE AND is_private = FALSE;

SELECT * FROM UserGroup AS g LEFT JOIN GroupMember gm ON 2 = gm.user_id;

SELECT g FROM UserGroup g JOIN g.users u ON u.id <> 1 OR g.users IS NULL WHERE g.validated = TRUE AND is_private = FALSE