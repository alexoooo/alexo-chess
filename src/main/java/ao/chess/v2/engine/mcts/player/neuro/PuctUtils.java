package ao.chess.v2.engine.mcts.player.neuro;


import ao.chess.v2.data.MovePicker;

import java.util.Random;


enum PuctUtils {
    ;


    public static void smearProbabilities(
            double[] probabilities,
            double amount
    ) {
        if (amount == 0) {
            return;
        }

        double total = 1 + amount;
        double each = amount / probabilities.length;

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = (probabilities[i] + each) / total;
        }
    }


    public static void smearProbabilities(
            double[] probabilities,
            double alpha,
            double signal,
            Random random,
            double[] buffer
    ) {
        double total = sampleGamma(alpha, 1.0, random, buffer, probabilities.length);

        int[] randomOrder = MovePicker.pickRandom(probabilities.length);

        for (int i = 0; i < probabilities.length; i++) {
            double dirichlet = buffer[randomOrder[i]] / total;
            probabilities[i] = signal * probabilities[i] + (1 - signal) * dirichlet;
        }
    }


    // https://en.wikipedia.org/wiki/Dirichlet_distribution
    // https://en.wikipedia.org/wiki/Simplex#The_standard_simplex
    // https://medium.com/oracledevs/lessons-from-alphazero-part-3-parameter-tweaking-4dceb78ed1e5
    // https://en.wikipedia.org/wiki/Gamma_distribution
    // https://github.com/EdwardRaff/JSAT/blob/master/JSAT/src/jsat/distributions/Gamma.java
    private static double sampleGamma(
            double k,
            double theta,
            Random random,
            double[] buffer,
            int count)
    {
        double total = 0;

        if (k >= 1.0)
        {
            double d = k - 1.0 / 3.0;
            double c = 1.0 / Math.sqrt(9.0*d);

            for (int i = 0; i < count; i++)
            {
                while (true)
                {
                    double x = 0;
                    double v = 0;

                    while (v <= 0.0)
                    {
                        x = random.nextGaussian();
                        v = 1 + c * x;
                    }

                    v = v * v * v;
                    double u = random.nextDouble();
                    double xSquared = x * x;

                    double squeezeCheck = 1.0 - 0.0331 * xSquared * xSquared;
                    if (u < squeezeCheck ||
                            Math.log(u) < 0.5 * xSquared + d * (1.0 - v + Math.log(v)))
                    {
                        double value = theta * d * v;
                        buffer[i] = value;
                        total += value;
                        break;
                    }
                }
            }
        }
        else
        {
            sampleGamma(k + 1, theta, random, buffer, count);

            for (int i = 0; i < count; i++) {
                double value = theta * buffer[i] * Math.pow(random.nextDouble(), 1.0 / k);
                buffer[i] = value;
                total += value;
            }
        }

        return total;
    }
}
