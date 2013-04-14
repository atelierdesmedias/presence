/* The mac adress was connected on this date and time */
create table adm_presence_stat (
  /* A mac adress is a 48-bit (6 bytes) integer */
  mac BIGINT(15) UNSIGNED NOT NULL,

  /* The time and date when this devide was connected */
  datetime DATETIME NOT NULL
)

/* Various informations associated to the max adress (should probably be in a wiki document to make it easier to manipulate) */
create table adm_presence_mac (
  /* A mac adress is a 48-bit (6 bytes) integer */
  mac BIGINT(15) UNSIGNED NOT NULL,

  /* The type of device */
  type SMALLINT UNSIGNED
)

