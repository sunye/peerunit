# Bird data converted to ranks and summed
birdRanks <- c(
35.5, 40.0, 36.0, 36.5, 38.5, 29.5, 35.5, 28.5,
48.0, 43.0, 37.5, 31.5, 29.5, 31.5, 29.5, 29.5,
39.0, 36.5, 43.5, 39.0, 28.5, 26.5, 36.5, 30.5,
41.5, 38.5, 32.5, 29.5, 34.5, 36.5, 33.5, 33.5,
43.0, 35.5, 37.0, 29.0, 31.5, 32.0, 36.0, 36.0,
39.0, 37.0, 36.0, 34.5, 28.5, 40.0, 33.0, 32.0,
);
days <- c('20050116', '20050130', '20050206',
  '20050213', '20050219', '20050227');
pools <- c('p37', 'p38', 'p39', 'p40', 'p41', 'p42', 'p43', 'p44');
birdMatrix <- matrix(birdRanks, nrow=6, ncol=8, byrow=TRUE,
  dimnames=list(days, pools));
average <- colMeans(birdMatrix);
average
combined <- rbind(birdMatrix, average)
tranBird <- t(combined)
dotchart(tranBird);
# The ranking system means that the sum of each pool's score for
# any particular day is constant, so there is no contribution from
# day in the Anova.
# information
pool=factor(rep(pools,6))
contrasts(pool) <- contr.sum(8)
forAnova <- data.frame(rank=birdRanks, pool=pool);
model <- lm(rank ~ pool, data = forAnova);
anova(model);
summary(model);
dummy.coef(model);
avgLocs <- c(2.5, 0.43182, 1, 1.6, 1.16667, 2.0, 0.8, 2.111111, 2.14286,
  4, 4, 2, 3.47368, 3.24000, 4.266667, 5.14286, 6.66667, 5.66667,
  0, 1.5, 0, 0, 0, 2, 1, 1.5, 1, 2, 0.5, 5, 0, 6, 6, 6, 6, 4.25, 5.75, 
  1.7895, 0.82353, 1.36842, 0, 1.3333, 0.111111);
avgBirds <- c('BHGull', 'BHGull', 'BHGull', 'Coot', 'Coot', 'Coot',
  'Coot', 'Coot', 'Coot', 'Heron', 'Heron', 'Kingfisher', 'Mallard',
  'Mallard', 'Mallard', 'Mallard', 'Mallard', 'Mallard', 'Moorhen',
  'Moorhen',
  'Moorhen', 'Moorhen', 'Moorhen', 'Pochard', 'Pochard', 'Pochard',
  'Pochard', 'Pochard', 'Smew', 'Smew', 'Smew', 'Swan', 'Swan',
  'Swan', 'Swan', 'Swan', 'Swan', 'TuftedDuck', 'TuftedDuck',
  'TuftedDuck', 'TuftedDuck', 'TuftedDuck', 'TuftedDuck');
avgFrame <- data.frame(avgLocs, avgBirds);
