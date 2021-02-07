create table MenuItem
(
	Id int identity 
        constraint PK_MenuItem
			primary key,
	Name        nvarchar(max) not null,
	Description nvarchar(max),
	Image       nvarchar(max),
	Price       float not null,
	CategoryId  int not null
		constraint FK_MenuItem_Category_CategoryId
			references Category
				on delete cascade,
	FoodTypeId  int not null
		constraint FK_MenuItem_FoodType_FoodTypeId
			references FoodType
				on delete cascade,
	Discount    decimal(15,3) default 0,
	Extra       varchar(30),
	Updated     timestamp null
)
go

create index IX_MenuItem_CategoryId
	on MenuItem (CategoryId)
go

create index IX_MenuItem_FoodTypeId
	on MenuItem (FoodTypeId)
go

