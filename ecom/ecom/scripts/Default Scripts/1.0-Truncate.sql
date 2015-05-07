/*Disable Constraints & Triggers*/
exec sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'
exec sp_MSforeachtable 'ALTER TABLE ? DISABLE TRIGGER ALL'
/*Perform delete operation on all table for cleanup*/
exec sp_MSforeachtable 'DELETE ?'
/*Enable Constraints & Triggers again*/
exec sp_MSforeachtable 'ALTER TABLE ? CHECK CONSTRAINT ALL'
exec sp_MSforeachtable 'ALTER TABLE ? ENABLE TRIGGER ALL'
/*Reset Identity on tables with identity column*/
exec sp_MSforeachtable 'IF OBJECTPROPERTY(OBJECT_ID(''?''), ''TableHasIdentity'') = 1 BEGIN DBCC CHECKIDENT (''?'',RESEED,0) END'
