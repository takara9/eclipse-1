CREATE DATABASE session
  CATALOG TABLESPACE
    MANAGED BY SYSTEM USING ('/tablespace')
  USER TABLESPACE
    MANAGED BY DATABASE USING (FILE '/tablespace/user' 5000)
  TEMPORARY TABLESPACE
    MANAGED BY SYSTEM USING ('/tablespace')
  WITH "comment";

