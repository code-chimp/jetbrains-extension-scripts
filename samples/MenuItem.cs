using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

[Table("MenuItem")]
public class MenuItem
{
    [Key]
    public int Id { get; set; }
    [Required]
    public string Name { get; set; }
    public string Description { get; set; }
    public string Image { get; set; }
    public double Price { get; set; }
    public int CategoryId { get; set; }
    public int FoodTypeId { get; set; }
    public decimal? Discount { get; set; }
    [MaxLength(30)]
    public string Extra { get; set; }
    [Timestamp]
    public byte[] Updated { get; set; }
}

