UPDATE sys_user
SET password_hash = '$2a$10$8vQEc2IWhJOsaPRyzU82mOepF2ci7f2VB2SXgNPuKlu94.dtMgLES'
WHERE username IN ('admin', 'user');
