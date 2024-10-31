package solo.project.entity;

import jakarta.persistence.*;

@Entity
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  private String writer;

}
