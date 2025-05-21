package com.rookies3.myspringbootlab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publishers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate establishedDate;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "publisher",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    //빌더 패턴을 적용했을때 변수에 명시적으로 초기화 한 값이 유지 되도록 해주는 어노테이션
    @Builder.Default
    private List<Book> books = new ArrayList<>();
}
