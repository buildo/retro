DROP TABLE IF EXISTS `access_token_auth_domain`;
CREATE TABLE `access_token_auth_domain` (
  `id` SERIAL NOT NULL,
  `ref` TEXT NOT NULL,
  `token` varchar(255) NOT NULL,
  `expires_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_UNIQUE` (`token`)
) DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `login_auth_domain`;
CREATE TABLE `login_auth_domain` (
  `id` SERIAL NOT NULL,
  `ref` TEXT NOT NULL,
  `username` varchar(255) NOT NULL,
  `password_hash` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) DEFAULT CHARSET=latin1;