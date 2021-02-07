export default interface IMenuItem {
  id: number;
  name: string;
  description: string;
  image: string;
  price: number;
  categoryId: number;
  foodTypeId: number;
  discount?: number;
  extra: string;
  updated?: Date;
}

